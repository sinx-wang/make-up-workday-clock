package com.example.data.repository

import com.example.data.local.AlarmDao
import com.example.data.local.AlarmGroupDao
import com.example.data.model.AlarmEntity
import com.example.data.model.AlarmGroupEntity
import com.example.data.model.HolidayRuleMode
import kotlinx.coroutines.flow.Flow

class AlarmRepository(
    private val alarmDao: AlarmDao,
    private val alarmGroupDao: AlarmGroupDao
) {
    val allGroupsFlow: Flow<List<AlarmGroupEntity>> = alarmGroupDao.observeAllGroups()
    val allAlarmsFlow: Flow<List<AlarmEntity>> = alarmDao.observeAllAlarms()

    suspend fun getAllGroupsOnce(): List<AlarmGroupEntity> = alarmGroupDao.getAllGroupsOnce()
    suspend fun getAllAlarmsOnce(): List<AlarmEntity> = alarmDao.getAllAlarmsOnce()
    suspend fun getAlarmById(id: Long): AlarmEntity? = alarmDao.getAlarmById(id)
    suspend fun getGroupById(id: Long): AlarmGroupEntity? = alarmGroupDao.getGroupById(id)

    suspend fun ensureStarterDataSeeded() {
        if (alarmGroupDao.countGroups() == 0 && alarmDao.countAlarms() == 0) {
            val workGroupId = alarmGroupDao.insertGroup(
                AlarmGroupEntity(
                    name = "早起通勤组",
                    description = "法定节假日自动关闭，调休补班周末自动开启",
                    iconKey = "work",
                    accentColorHex = 0xFF387AFF,
                    isEnabled = true,
                    defaultRuleMode = HolidayRuleMode.LEGAL_WORKDAY.name,
                    includeLieuWorkdays = true,
                    sortOrder = 0
                )
            )
            val holidayGroupId = alarmGroupDao.insertGroup(
                AlarmGroupEntity(
                    name = "节假慵懒组",
                    description = "仅周末与法定节假日开启，调休补班自动关闭",
                    iconKey = "holiday",
                    accentColorHex = 0xFFFF5A5F,
                    isEnabled = true,
                    defaultRuleMode = HolidayRuleMode.LEGAL_HOLIDAY_AND_WEEKEND.name,
                    includeLieuWorkdays = false,
                    sortOrder = 1
                )
            )
            val fitnessGroupId = alarmGroupDao.insertGroup(
                AlarmGroupEntity(
                    name = "规律打卡组",
                    description = "自定义星期并自动避开法定节假日",
                    iconKey = "fitness",
                    accentColorHex = 0xFF10B981,
                    isEnabled = false,
                    defaultRuleMode = HolidayRuleMode.CUSTOM_SKIP_HOLIDAY.name,
                    customDaysMask = 0b0010101, // 周一、周三、周五
                    includeLieuWorkdays = false,
                    sortOrder = 2
                )
            )

            alarmDao.insertAlarm(
                AlarmEntity(
                    groupId = workGroupId,
                    hour = 7,
                    minute = 20,
                    label = "起床洗漱",
                    isEnabled = true,
                    inheritGroupRule = true,
                    ruleMode = HolidayRuleMode.LEGAL_WORKDAY.name,
                    vibrate = true,
                    gradualVolume = true,
                    snoozeMinutes = 5
                )
            )
            alarmDao.insertAlarm(
                AlarmEntity(
                    groupId = workGroupId,
                    hour = 7,
                    minute = 50,
                    label = "出发打卡提醒",
                    isEnabled = true,
                    inheritGroupRule = true,
                    ruleMode = HolidayRuleMode.LEGAL_WORKDAY.name,
                    vibrate = true,
                    gradualVolume = true,
                    snoozeMinutes = 5
                )
            )
            alarmDao.insertAlarm(
                AlarmEntity(
                    groupId = holidayGroupId,
                    hour = 9,
                    minute = 30,
                    label = "假日早午餐",
                    isEnabled = true,
                    inheritGroupRule = true,
                    ruleMode = HolidayRuleMode.LEGAL_HOLIDAY_AND_WEEKEND.name,
                    vibrate = true,
                    gradualVolume = true,
                    snoozeMinutes = 10
                )
            )
            alarmDao.insertAlarm(
                AlarmEntity(
                    groupId = fitnessGroupId,
                    hour = 19,
                    minute = 0,
                    label = "晚间跑步热身",
                    isEnabled = true,
                    inheritGroupRule = true,
                    ruleMode = HolidayRuleMode.CUSTOM_SKIP_HOLIDAY.name,
                    customDaysMask = 0b0010101,
                    includeLieuWorkdays = false,
                    vibrate = true,
                    gradualVolume = false,
                    snoozeMinutes = 5
                )
            )
        }
    }

    suspend fun saveGroup(group: AlarmGroupEntity): Long {
        return if (group.id == 0L) {
            alarmGroupDao.insertGroup(group)
        } else {
            alarmGroupDao.updateGroup(group)
            group.id
        }
    }

    suspend fun deleteGroup(group: AlarmGroupEntity) {
        alarmGroupDao.deleteGroup(group)
    }

    suspend fun setGroupEnabled(groupId: Long, enabled: Boolean) {
        alarmGroupDao.setGroupEnabled(groupId, enabled)
    }

    suspend fun setGroupSkippedDate(groupId: Long, dateIso: String?) {
        alarmGroupDao.setGroupSkippedDate(groupId, dateIso)
    }

    suspend fun saveAlarm(alarm: AlarmEntity): Long {
        return if (alarm.id == 0L) {
            alarmDao.insertAlarm(alarm)
        } else {
            alarmDao.updateAlarm(alarm)
            alarm.id
        }
    }

    suspend fun deleteAlarm(alarm: AlarmEntity) {
        alarmDao.deleteAlarm(alarm)
    }

    suspend fun setAlarmEnabled(alarmId: Long, enabled: Boolean) {
        alarmDao.setAlarmEnabled(alarmId, enabled)
    }

    suspend fun setSkippedDate(alarmId: Long, dateIso: String?) {
        alarmDao.setSkippedDate(alarmId, dateIso)
    }
}
