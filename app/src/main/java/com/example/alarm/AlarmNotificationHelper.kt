package com.example.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.model.ChinaDayType
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.ZhCnStrings

object AlarmNotificationHelper {
    const val CHANNEL_ALARM_RING = "holiday_alarm_ring_channel"
    const val CHANNEL_SYNC_STATUS = "holiday_sync_status_channel"
    const val NOTIFICATION_ID_RINGING = 9001

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return

            val ringChannel = NotificationChannel(
                CHANNEL_ALARM_RING,
                context.getString(R.string.channel_alarm_ring_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_alarm_ring_desc)
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null) // Audio handled by AlarmRingingService with USAGE_ALARM
                enableVibration(false)
            }

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC_STATUS,
                context.getString(R.string.channel_sync_status_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.channel_sync_status_desc)
            }

            manager.createNotificationChannel(ringChannel)
            manager.createNotificationChannel(syncChannel)
        }
    }

    fun buildRingingNotification(
        context: Context,
        alarmId: Long,
        label: String,
        timeText: String,
        dayBadge: String,
        snoozeMinutes: Int,
        strings: AppStrings = ZhCnStrings
    ): Notification {
        ensureChannels(context)

        val fullScreenIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmConstants.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmConstants.EXTRA_ALARM_TIME, timeText)
            putExtra(AlarmConstants.EXTRA_DAY_BADGE, dayBadge)
            putExtra(AlarmConstants.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt() + 10000,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, AlarmRingingService::class.java).apply {
            action = AlarmConstants.ACTION_DISMISS
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getService(
            context,
            alarmId.toInt() + 20000,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, AlarmRingingService::class.java).apply {
            action = AlarmConstants.ACTION_SNOOZE
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmConstants.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmConstants.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
        }
        val snoozePendingIntent = PendingIntent.getService(
            context,
            alarmId.toInt() + 30000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "$timeText · ${label.ifBlank { strings.appTitle }}"
        val localizedBadge = if (dayBadge.isNotBlank()) {
            strings.localizeHolidayLabel(dayBadge, ChinaDayType.WORKDAY)
        } else {
            ""
        }
        val body = if (localizedBadge.isNotBlank()) strings.notifTodayBody(localizedBadge) else strings.notifDefaultBody

        return NotificationCompat.Builder(context, CHANNEL_ALARM_RING)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .addAction(0, strings.notifSnoozeAction(snoozeMinutes), snoozePendingIntent)
            .addAction(0, strings.dismissAlarm, dismissPendingIntent)
            .build()
    }
}

object AlarmConstants {
    const val ACTION_FIRE_ALARM = "com.example.action.FIRE_ALARM"
    const val ACTION_DISMISS = "com.example.action.DISMISS_ALARM"
    const val ACTION_SNOOZE = "com.example.action.SNOOZE_ALARM"
    const val ACTION_TEST_RING = "com.example.action.TEST_RING"

    const val EXTRA_ALARM_ID = "extra_alarm_id"
    const val EXTRA_ALARM_LABEL = "extra_alarm_label"
    const val EXTRA_ALARM_TIME = "extra_alarm_time"
    const val EXTRA_DAY_BADGE = "extra_day_badge"
    const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
    const val EXTRA_VIBRATE = "extra_vibrate"
    const val EXTRA_GRADUAL_VOLUME = "extra_gradual_volume"
    const val EXTRA_RINGTONE_URI = "extra_ringtone_uri"
    const val EXTRA_IS_SNOOZE = "extra_is_snooze"
    const val EXTRA_IS_TEST = "extra_is_test"
}
