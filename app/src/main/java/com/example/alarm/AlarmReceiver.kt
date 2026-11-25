package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.HolidayAlarmApp
import com.example.data.model.HolidayRuleMode
import com.example.data.model.SimpleDate
import com.example.domain.AlarmScheduleCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Receives AlarmManager fire events, double-checks today's latest holiday/lieu-workday state
 * before launching AlarmRingingService, and automatically schedules the next valid occurrence.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmConstants.ACTION_FIRE_ALARM) return

        val pendingResult = goAsync()
        val app = context.applicationContext as HolidayAlarmApp
        val alarmId = intent.getLongExtra(AlarmConstants.EXTRA_ALARM_ID, -1L)
        val isSnooze = intent.getBooleanExtra(AlarmConstants.EXTRA_IS_SNOOZE, false)

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                if (isSnooze) {
                    startRingingService(context, intent)
                    return@launch
                }

                val alarm = app.alarmRepository.getAlarmById(alarmId)
                if (alarm == null) {
                    app.alarmScheduler.rescheduleAll()
                    return@launch
                }

                val group = alarm.groupId?.let { app.alarmRepository.getGroupById(it) }
                val holidayMap = app.holidayRepository.getHolidayMapOnce()
                val today = SimpleDate.today()

                // Real-time holiday verification right before ringing
                val shouldRing = AlarmScheduleCalculator.shouldRingOnDate(
                    alarm = alarm,
                    group = group,
                    date = today,
                    holidayMap = holidayMap
                )

                if (shouldRing) {
                    val dayInfo = AlarmScheduleCalculator.resolveDay(today, holidayMap)
                    val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
                        action = AlarmConstants.ACTION_FIRE_ALARM
                        putExtra(AlarmConstants.EXTRA_ALARM_ID, alarm.id)
                        putExtra(AlarmConstants.EXTRA_ALARM_LABEL, alarm.label)
                        putExtra(AlarmConstants.EXTRA_ALARM_TIME, alarm.formattedTime())
                        putExtra(AlarmConstants.EXTRA_DAY_BADGE, dayInfo.label)
                        putExtra(AlarmConstants.EXTRA_SNOOZE_MINUTES, alarm.snoozeMinutes)
                        putExtra(AlarmConstants.EXTRA_VIBRATE, alarm.vibrate)
                        putExtra(AlarmConstants.EXTRA_GRADUAL_VOLUME, alarm.gradualVolume)
                        putExtra(AlarmConstants.EXTRA_RINGTONE_URI, alarm.ringtoneUri)
                    }
                    ContextCompat.startForegroundService(context, serviceIntent)

                    // Disable ONE-SHOT alarms after firing
                    if (alarm.effectiveRuleMode(group) == HolidayRuleMode.ONCE) {
                        app.alarmRepository.setAlarmEnabled(alarm.id, false)
                    }
                }

                // Clear expired skip flag if needed and schedule next future occurrence
                if (alarm.skippedDateIso == today.toIsoString()) {
                    app.alarmRepository.setSkippedDate(alarm.id, null)
                }
                app.alarmScheduler.rescheduleAll()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun startRingingService(context: Context, sourceIntent: Intent) {
        val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
            action = AlarmConstants.ACTION_FIRE_ALARM
            putExtras(sourceIntent)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
