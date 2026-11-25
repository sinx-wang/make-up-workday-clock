package com.example

import android.app.Application
import com.example.alarm.AlarmNotificationHelper
import com.example.alarm.AlarmScheduler
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesRepository
import com.example.data.repository.AlarmRepository
import com.example.data.repository.HolidayRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HolidayAlarmApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val preferencesRepository: PreferencesRepository by lazy { PreferencesRepository(this) }
    val holidayRepository: HolidayRepository by lazy {
        HolidayRepository(database.holidayDao(), preferencesRepository)
    }
    val alarmRepository: AlarmRepository by lazy {
        AlarmRepository(database.alarmDao(), database.alarmGroupDao())
    }
    val alarmScheduler: AlarmScheduler by lazy {
        AlarmScheduler(this, alarmRepository, holidayRepository)
    }

    override fun onCreate() {
        super.onCreate()
        AlarmNotificationHelper.ensureChannels(this)
        applicationScope.launch {
            holidayRepository.ensureSeeded()
            alarmRepository.ensureStarterDataSeeded()
            alarmScheduler.rescheduleAll()
            // Perform silent auto-sync check if interval has elapsed
            holidayRepository.syncIfDue()
            alarmScheduler.rescheduleAll()
        }
    }
}
