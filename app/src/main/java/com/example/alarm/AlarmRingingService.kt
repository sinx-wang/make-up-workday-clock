package com.example.alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.HolidayAlarmApp
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringPacks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Foreground service (mediaPlayback type) responsible for reliable alarm playback,
 * gradual One UI volume crescendo, rhythmic vibration, and dismiss/snooze controls.
 */
class AlarmRingingService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var rampJob: Job? = null

    private var activeAlarmId: Long = -1L
    private var activeLabel: String = "节律闹钟"
    private var activeSnoozeMinutes: Int = 5
    private var activeVibrate: Boolean = true
    private var activeGradualVolume: Boolean = true
    private var activeRingtoneUri: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: AlarmConstants.ACTION_FIRE_ALARM

        when (action) {
            AlarmConstants.ACTION_DISMISS -> {
                stopRingingAndFinish()
                return START_NOT_STICKY
            }
            AlarmConstants.ACTION_SNOOZE -> {
                val alarmId = intent?.getLongExtra(AlarmConstants.EXTRA_ALARM_ID, activeAlarmId) ?: activeAlarmId
                val label = intent?.getStringExtra(AlarmConstants.EXTRA_ALARM_LABEL) ?: activeLabel
                val snoozeMinutes = intent?.getIntExtra(AlarmConstants.EXTRA_SNOOZE_MINUTES, activeSnoozeMinutes)
                    ?: activeSnoozeMinutes
                val app = applicationContext as HolidayAlarmApp
                if (alarmId > 0L) {
                    app.alarmScheduler.scheduleSnooze(
                        alarmId = alarmId,
                        label = label,
                        snoozeMinutes = snoozeMinutes,
                        vibrate = activeVibrate,
                        gradualVolume = activeGradualVolume,
                        ringtoneUri = activeRingtoneUri
                    )
                }
                stopRingingAndFinish()
                return START_NOT_STICKY
            }
            AlarmConstants.ACTION_FIRE_ALARM, AlarmConstants.ACTION_TEST_RING -> {
                val app = applicationContext as HolidayAlarmApp
                val appLanguage = runCatching {
                    runBlocking(Dispatchers.IO) { app.preferencesRepository.preferencesFlow.first().appLanguage }
                }.getOrDefault(AppLanguage.ZH_CN)
                val strings = StringPacks.forLanguage(appLanguage)

                activeAlarmId = intent?.getLongExtra(AlarmConstants.EXTRA_ALARM_ID, -1L) ?: -1L
                activeLabel = intent?.getStringExtra(AlarmConstants.EXTRA_ALARM_LABEL).orEmpty().ifBlank { strings.appTitle }
                val timeText = intent?.getStringExtra(AlarmConstants.EXTRA_ALARM_TIME).orEmpty().ifBlank { "07:30" }
                val dayBadge = intent?.getStringExtra(AlarmConstants.EXTRA_DAY_BADGE).orEmpty()
                activeSnoozeMinutes = intent?.getIntExtra(AlarmConstants.EXTRA_SNOOZE_MINUTES, 5) ?: 5
                activeVibrate = intent?.getBooleanExtra(AlarmConstants.EXTRA_VIBRATE, true) ?: true
                activeGradualVolume = intent?.getBooleanExtra(AlarmConstants.EXTRA_GRADUAL_VOLUME, true) ?: true
                activeRingtoneUri = intent?.getStringExtra(AlarmConstants.EXTRA_RINGTONE_URI)

                val notification = AlarmNotificationHelper.buildRingingNotification(
                    context = this,
                    alarmId = activeAlarmId,
                    label = activeLabel,
                    timeText = timeText,
                    dayBadge = dayBadge,
                    snoozeMinutes = activeSnoozeMinutes,
                    strings = strings
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        AlarmNotificationHelper.NOTIFICATION_ID_RINGING,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    startForeground(AlarmNotificationHelper.NOTIFICATION_ID_RINGING, notification)
                }

                startAlarmAudio(activeRingtoneUri, activeGradualVolume)
                if (activeVibrate) startVibration()

                // Launch One UI 9 full-screen ringing screen
                val activityIntent = Intent(this, AlarmRingingActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra(AlarmConstants.EXTRA_ALARM_ID, activeAlarmId)
                    putExtra(AlarmConstants.EXTRA_ALARM_LABEL, activeLabel)
                    putExtra(AlarmConstants.EXTRA_ALARM_TIME, timeText)
                    putExtra(AlarmConstants.EXTRA_DAY_BADGE, dayBadge)
                    putExtra(AlarmConstants.EXTRA_SNOOZE_MINUTES, activeSnoozeMinutes)
                    putExtra(AlarmConstants.EXTRA_IS_TEST, action == AlarmConstants.ACTION_TEST_RING)
                }
                runCatching { startActivity(activityIntent) }
            }
        }
        return START_NOT_STICKY
    }

    private fun startAlarmAudio(ringtoneUriStr: String?, gradualVolume: Boolean) {
        stopAudio()
        val uri = ringtoneUriStr?.let { runCatching { Uri.parse(it) }.getOrNull() }
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: return

        runCatching {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@AlarmRingingService, uri)
                isLooping = true
                prepare()
                if (gradualVolume) {
                    setVolume(0.12f, 0.12f)
                } else {
                    setVolume(1.0f, 1.0f)
                }
                start()
            }
            mediaPlayer = player

            if (gradualVolume) {
                rampJob = serviceScope.launch {
                    val steps = 20
                    for (i in 1..steps) {
                        delay(750L)
                        val vol = (0.12f + 0.88f * (i.toFloat() / steps)).coerceAtMost(1.0f)
                        runCatching { mediaPlayer?.setVolume(vol, vol) }
                    }
                }
            }
        }
    }

    private fun startVibration() {
        stopVibration()
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        vibrator = vib
        val timings = longArrayOf(0, 500, 400, 500, 900)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib?.vibrate(VibrationEffect.createWaveform(timings, 0))
        } else {
            @Suppress("DEPRECATION")
            vib?.vibrate(timings, 0)
        }
    }

    private fun stopAudio() {
        rampJob?.cancel()
        rampJob = null
        runCatching {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
        mediaPlayer = null
    }

    private fun stopVibration() {
        runCatching { vibrator?.cancel() }
        vibrator = null
    }

    private fun stopRingingAndFinish() {
        stopAudio()
        stopVibration()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopAudio()
        stopVibration()
        serviceScope.cancel()
        super.onDestroy()
    }
}
