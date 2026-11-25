package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.HolidayAlarmApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Automatically reschedules all holiday-aware alarms after reboot, time/date change,
 * timezone change, or exact-alarm permission changes.
 */
class BootRescheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val supported = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
        )
        if (action !in supported) return

        val pendingResult = goAsync()
        val app = context.applicationContext as HolidayAlarmApp
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                app.holidayRepository.ensureSeeded()
                app.alarmScheduler.rescheduleAll()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
