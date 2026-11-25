package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.MainActivity
import com.example.data.model.AlarmEntity
import com.example.data.model.AlarmGroupEntity
import com.example.data.model.HolidayDayEntity
import com.example.data.repository.AlarmRepository
import com.example.data.repository.HolidayRepository
import com.example.domain.AlarmScheduleCalculator
import com.example.domain.NextAlarmOccurrence
import java.util.Locale

/**
 * Schedules exact alarms via Android AlarmManager.setAlarmClock().
 * Using setAlarmClock() ensures Doze-mode immunity and automatic integration with
 * Samsung Galaxy S26 Ultra / One UI 9 Status Bar, Always On Display, Lockscreen, and Now Bar.
 */
class AlarmScheduler(
    private val context: Context,
    private val alarmRepository: AlarmRepository,
    private val holidayRepository: HolidayRepository
) {
    private val alarmManager: AlarmManager? =
        context.getSystemService(AlarmManager::class.java)

    fun canScheduleExactAlarms(): Boolean {
        val manager = alarmManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            manager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    suspend fun rescheduleAll(): NextAlarmOccurrence? {
        val alarms = alarmRepository.getAllAlarmsOnce()
        val groupsById = alarmRepository.getAllGroupsOnce().associateBy { it.id }
        val holidayMap = holidayRepository.getHolidayMapOnce()
        val nowMillis = System.currentTimeMillis()

        var earliest: NextAlarmOccurrence? = null

        for (alarm in alarms) {
            val group = alarm.groupId?.let { groupsById[it] }
            val next = AlarmScheduleCalculator.computeNextOccurrence(
                alarm = alarm,
                group = group,
                holidayMap = holidayMap,
                nowMillis = nowMillis
            )
            if (next != null) {
                scheduleSingleOccurrence(next)
                if (earliest == null || next.triggerMillis < earliest.triggerMillis) {
                    earliest = next
                }
            } else {
                cancelAlarm(alarm.id)
            }
        }
        return earliest
    }

    fun scheduleSingleOccurrence(occurrence: NextAlarmOccurrence) {
        val manager = alarmManager ?: return
        val alarm = occurrence.alarm

        val fireIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmConstants.ACTION_FIRE_ALARM
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmConstants.EXTRA_ALARM_LABEL, alarm.label)
            putExtra(AlarmConstants.EXTRA_ALARM_TIME, alarm.formattedTime())
            putExtra(AlarmConstants.EXTRA_DAY_BADGE, occurrence.dayInfo.label)
            putExtra(AlarmConstants.EXTRA_SNOOZE_MINUTES, alarm.snoozeMinutes)
            putExtra(AlarmConstants.EXTRA_VIBRATE, alarm.vibrate)
            putExtra(AlarmConstants.EXTRA_GRADUAL_VOLUME, alarm.gradualVolume)
            putExtra(AlarmConstants.EXTRA_RINGTONE_URI, alarm.ringtoneUri)
            putExtra(AlarmConstants.EXTRA_IS_SNOOZE, false)
        }

        val operationPendingIntent = PendingIntent.getBroadcast(
            context,
            requestCodeForAlarm(alarm.id),
            fireIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestCodeForAlarm(alarm.id) + 50000,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (canScheduleExactAlarms()) {
                val clockInfo = AlarmManager.AlarmClockInfo(occurrence.triggerMillis, showPendingIntent)
                manager.setAlarmClock(clockInfo, operationPendingIntent)
            } else {
                // Graceful fallback when exact alarm permission is not yet granted
                manager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    occurrence.triggerMillis,
                    operationPendingIntent
                )
            }
        } catch (se: SecurityException) {
            Log.w(TAG, "Exact alarm permission denied, falling back to inexact alarm", se)
            manager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                occurrence.triggerMillis,
                operationPendingIntent
            )
        }
    }

    fun scheduleSnooze(
        alarmId: Long,
        label: String,
        snoozeMinutes: Int,
        vibrate: Boolean,
        gradualVolume: Boolean,
        ringtoneUri: String?
    ) {
        val manager = alarmManager ?: return
        val triggerMillis = System.currentTimeMillis() + snoozeMinutes * 60_000L
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = triggerMillis }
        val timeText = String.format(
            Locale.US,
            "%02d:%02d",
            cal.get(java.util.Calendar.HOUR_OF_DAY),
            cal.get(java.util.Calendar.MINUTE)
        )

        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmConstants.ACTION_FIRE_ALARM
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmConstants.EXTRA_ALARM_LABEL, "$label（稍后提醒）")
            putExtra(AlarmConstants.EXTRA_ALARM_TIME, timeText)
            putExtra(AlarmConstants.EXTRA_DAY_BADGE, "稍后提醒")
            putExtra(AlarmConstants.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
            putExtra(AlarmConstants.EXTRA_VIBRATE, vibrate)
            putExtra(AlarmConstants.EXTRA_GRADUAL_VOLUME, gradualVolume)
            putExtra(AlarmConstants.EXTRA_RINGTONE_URI, ringtoneUri)
            putExtra(AlarmConstants.EXTRA_IS_SNOOZE, true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCodeForSnooze(alarmId),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestCodeForSnooze(alarmId) + 50000,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (canScheduleExactAlarms()) {
                manager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerMillis, showPendingIntent), pendingIntent)
            } else {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } catch (se: SecurityException) {
            Log.w(TAG, "Unable to schedule exact snooze", se)
        }
    }

    fun cancelAlarm(alarmId: Long) {
        val manager = alarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmConstants.ACTION_FIRE_ALARM
        }
        val pi = PendingIntent.getBroadcast(
            context,
            requestCodeForAlarm(alarmId),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) {
            manager.cancel(pi)
            pi.cancel()
        }
    }

    private fun requestCodeForAlarm(alarmId: Long): Int = (alarmId % 40000L).toInt() + 100
    private fun requestCodeForSnooze(alarmId: Long): Int = (alarmId % 40000L).toInt() + 45000

    companion object {
        private const val TAG = "AlarmScheduler"
    }
}
