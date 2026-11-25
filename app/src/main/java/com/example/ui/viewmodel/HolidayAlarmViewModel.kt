package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.HolidayAlarmApp
import com.example.alarm.AlarmConstants
import com.example.alarm.AlarmRingingService
import com.example.data.local.AppPreferences
import com.example.data.local.AutoSyncInterval
import com.example.data.local.ThemeMode
import com.example.data.local.ThemePalette
import com.example.data.model.AlarmEntity
import com.example.data.model.AlarmGroupEntity
import com.example.data.model.ChinaDayType
import com.example.data.model.HolidayDayEntity
import com.example.data.model.ResolvedDayInfo
import com.example.data.model.SimpleDate
import com.example.data.repository.HolidaySyncResult
import com.example.domain.AlarmScheduleCalculator
import com.example.domain.NextAlarmOccurrence
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.StringPacks
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AlarmItemUiModel(
    val alarm: AlarmEntity,
    val group: AlarmGroupEntity?,
    val nextOccurrence: NextAlarmOccurrence?,
    val todayStatusLabel: String,
    val tomorrowStatusLabel: String,
    val isAutoPausedTomorrow: Boolean,
    val activeSkippedDateLabel: String? = null
)

data class AlarmGroupUiModel(
    val group: AlarmGroupEntity,
    val alarms: List<AlarmItemUiModel>,
    val remainingTodayCount: Int = 0,
    val activeTomorrowCount: Int,
    val autoPausedTomorrowCount: Int,
    val nextTriggerDate: SimpleDate? = null,
    val activeSkippedDateLabel: String? = null
)

data class HolidayAlarmUiState(
    val todayInfo: ResolvedDayInfo = ResolvedDayInfo(SimpleDate.today(), ChinaDayType.WORKDAY, "工作日"),
    val tomorrowInfo: ResolvedDayInfo = ResolvedDayInfo(SimpleDate.today().plusDays(1), ChinaDayType.WORKDAY, "工作日"),
    val nextGlobalOccurrence: NextAlarmOccurrence? = null,
    val nextCountdownText: String = "暂无已开启闹钟",
    val groups: List<AlarmGroupUiModel> = emptyList(),
    val ungroupedAlarms: List<AlarmItemUiModel> = emptyList(),
    val holidayMap: Map<String, HolidayDayEntity> = emptyMap(),
    val preferences: AppPreferences = AppPreferences(),
    val isSyncing: Boolean = false,
    val userOverrideCount: Int = 0
)

class HolidayAlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as HolidayAlarmApp
    private val alarmRepo = app.alarmRepository
    private val holidayRepo = app.holidayRepository
    private val prefsRepo = app.preferencesRepository
    private val scheduler = app.alarmScheduler

    private val _isSyncing = MutableStateFlow(false)
    private val _clockTick = MutableStateFlow(System.currentTimeMillis())
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val uiState: StateFlow<HolidayAlarmUiState> = combine(
        alarmRepo.allGroupsFlow,
        alarmRepo.allAlarmsFlow,
        holidayRepo.holidayMapFlow,
        prefsRepo.preferencesFlow,
        combine(_isSyncing, _clockTick) { syncing, tick -> syncing to tick }
    ) { groups, alarms, holidayMap, prefs, (isSyncing, nowMillis) ->
        val strings = StringPacks.forLanguage(prefs.appLanguage)
        val today = SimpleDate.fromMillis(nowMillis)
        val tomorrow = today.plusDays(1)
        val todayInfo = AlarmScheduleCalculator.resolveDay(today, holidayMap)
        val tomorrowInfo = AlarmScheduleCalculator.resolveDay(tomorrow, holidayMap)
        val groupsById = groups.associateBy { it.id }

        val alarmUiModels = alarms.map { alarm ->
            val group = alarm.groupId?.let { groupsById[it] }
            val next = AlarmScheduleCalculator.computeNextOccurrence(
                alarm = alarm,
                group = group,
                holidayMap = holidayMap,
                nowMillis = nowMillis
            )
            val ringsToday = AlarmScheduleCalculator.shouldRingOnDate(alarm, group, today, holidayMap)
            val ringsTomorrow = AlarmScheduleCalculator.shouldRingOnDate(alarm, group, tomorrow, holidayMap)
            val todaySkip = strings.skipReasonForDate(alarm, group, today, holidayMap)
            val tomorrowSkip = strings.skipReasonForDate(alarm, group, tomorrow, holidayMap)

            val alarmSkippedDate = alarm.skippedDateIso?.let { SimpleDate.parseIso(it) }
            val activeAlarmSkippedLabel = if (alarmSkippedDate != null && alarmSkippedDate >= today) {
                strings.formatSkippedDateWord(today, alarmSkippedDate)
            } else null

            AlarmItemUiModel(
                alarm = alarm,
                group = group,
                nextOccurrence = next,
                todayStatusLabel = if (ringsToday) strings.todayWord else (todaySkip ?: strings.groupDisabled),
                tomorrowStatusLabel = if (ringsTomorrow) strings.tomorrowWord else (tomorrowSkip ?: strings.tomorrowAutoSkipped),
                isAutoPausedTomorrow = alarm.isEffectivelyEnabled(group) && !ringsTomorrow,
                activeSkippedDateLabel = activeAlarmSkippedLabel
            )
        }

        val groupUiModels = groups.map { group ->
            val groupAlarms = alarmUiModels.filter { it.alarm.groupId == group.id }
            val remainingToday = groupAlarms.count { it.nextOccurrence?.triggerDate == today }
            val activeTomorrow = groupAlarms.count {
                it.alarm.isEffectivelyEnabled(group) && !it.isAutoPausedTomorrow
            }
            val pausedTomorrow = groupAlarms.count { it.isAutoPausedTomorrow }
            val earliestTriggerDate = groupAlarms.mapNotNull { it.nextOccurrence?.triggerDate }.minOrNull()
            val skippedDate = group.skippedDateIso?.let { SimpleDate.parseIso(it) }
            val activeSkippedLabel = if (skippedDate != null && skippedDate >= today) {
                strings.formatSkippedDateWord(today, skippedDate)
            } else null

            AlarmGroupUiModel(
                group = group,
                alarms = groupAlarms,
                remainingTodayCount = remainingToday,
                activeTomorrowCount = activeTomorrow,
                autoPausedTomorrowCount = pausedTomorrow,
                nextTriggerDate = earliestTriggerDate,
                activeSkippedDateLabel = activeSkippedLabel
            )
        }

        val ungrouped = alarmUiModels.filter { it.alarm.groupId == null || groupsById[it.alarm.groupId] == null }

        val earliest = alarmUiModels
            .mapNotNull { it.nextOccurrence }
            .minByOrNull { it.triggerMillis }

        val countdown = if (earliest != null) {
            strings.formatCountdown(earliest.triggerMillis, nowMillis)
        } else {
            strings.noActiveAlarms
        }

        HolidayAlarmUiState(
            todayInfo = todayInfo,
            tomorrowInfo = tomorrowInfo,
            nextGlobalOccurrence = earliest,
            nextCountdownText = countdown,
            groups = groupUiModels,
            ungroupedAlarms = ungrouped,
            holidayMap = holidayMap,
            preferences = prefs,
            isSyncing = isSyncing,
            userOverrideCount = holidayMap.values.count { it.isUserOverride }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HolidayAlarmUiState()
    )

    init {
        viewModelScope.launch {
            while (true) {
                delay(30_000L)
                _clockTick.value = System.currentTimeMillis()
            }
        }
    }

    fun consumeSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun toggleAlarm(alarmId: Long, enabled: Boolean) {
        viewModelScope.launch {
            alarmRepo.setAlarmEnabled(alarmId, enabled)
            scheduler.rescheduleAll()
            _clockTick.value = System.currentTimeMillis()
        }
    }

    private fun currentStrings(): AppStrings =
        StringPacks.forLanguage(uiState.value.preferences.appLanguage)

    fun toggleGroup(groupId: Long, enabled: Boolean) {
        viewModelScope.launch {
            val s = currentStrings()
            val group = alarmRepo.getGroupById(groupId)
            alarmRepo.setGroupEnabled(groupId, enabled)
            if (enabled && group?.ruleModeEnum == com.example.data.model.HolidayRuleMode.ONCE) {
                val childAlarms = alarmRepo.getAllAlarmsOnce().filter { it.groupId == groupId }
                if (childAlarms.isNotEmpty() && childAlarms.none { it.isEnabled }) {
                    childAlarms.forEach { alarmRepo.setAlarmEnabled(it.id, true) }
                }
            }
            scheduler.rescheduleAll()
            _clockTick.value = System.currentTimeMillis()
            showMessage(if (enabled) s.msgGroupEnabled else s.msgGroupDisabled)
        }
    }

    fun toggleSkipNextForGroup(groupModel: AlarmGroupUiModel) {
        viewModelScope.launch {
            val s = currentStrings()
            val group = groupModel.group
            if (groupModel.activeSkippedDateLabel != null) {
                alarmRepo.setGroupSkippedDate(group.id, null)
                scheduler.rescheduleAll()
                _clockTick.value = System.currentTimeMillis()
                showMessage(s.msgGroupSkipRestored(group.name))
            } else {
                val earliest = groupModel.alarms
                    .mapNotNull { it.nextOccurrence }
                    .minByOrNull { it.triggerMillis }
                    ?: return@launch
                alarmRepo.setGroupSkippedDate(group.id, earliest.triggerDate.toIsoString())
                scheduler.rescheduleAll()
                _clockTick.value = System.currentTimeMillis()
                showMessage(s.msgGroupSkipped(group.name, s.formatShortDate(earliest.triggerDate)))
            }
        }
    }

    fun toggleSkipNextOccurrence(item: AlarmItemUiModel) {
        viewModelScope.launch {
            val s = currentStrings()
            val alarm = item.alarm
            if (item.activeSkippedDateLabel != null) {
                alarmRepo.setSkippedDate(alarm.id, null)
                scheduler.rescheduleAll()
                showMessage(s.msgAlarmSkipRestored)
            } else {
                val nextDateIso = item.nextOccurrence?.triggerDate?.toIsoString() ?: return@launch
                alarmRepo.setSkippedDate(alarm.id, nextDateIso)
                scheduler.rescheduleAll()
                showMessage(s.msgAlarmSkipped(s.formatShortDate(item.nextOccurrence.triggerDate)))
            }
            _clockTick.value = System.currentTimeMillis()
        }
    }

    fun saveAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val s = currentStrings()
            alarmRepo.saveAlarm(alarm)
            scheduler.rescheduleAll()
            _clockTick.value = System.currentTimeMillis()
            showMessage(s.msgAlarmSaved)
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val s = currentStrings()
            scheduler.cancelAlarm(alarm.id)
            alarmRepo.deleteAlarm(alarm)
            scheduler.rescheduleAll()
            _clockTick.value = System.currentTimeMillis()
            showMessage(s.msgAlarmDeleted(alarm.label.ifBlank { alarm.formattedTime() }))
        }
    }

    fun saveGroup(group: AlarmGroupEntity) {
        viewModelScope.launch {
            val s = currentStrings()
            alarmRepo.saveGroup(group)
            scheduler.rescheduleAll()
            _clockTick.value = System.currentTimeMillis()
            showMessage(s.msgGroupSaved(group.name))
        }
    }

    fun deleteGroup(group: AlarmGroupEntity) {
        viewModelScope.launch {
            val s = currentStrings()
            alarmRepo.deleteGroup(group)
            scheduler.rescheduleAll()
            _clockTick.value = System.currentTimeMillis()
            showMessage(s.msgGroupDeleted)
        }
    }

    fun syncHolidaysNow(years: List<Int> = listOf(SimpleDate.today().year, SimpleDate.today().year + 1)) {
        if (_isSyncing.value) return
        viewModelScope.launch {
            val s = currentStrings()
            _isSyncing.value = true
            try {
                when (val result = holidayRepo.syncYearsFromNetwork(years)) {
                    is HolidaySyncResult.Success -> {
                        scheduler.rescheduleAll()
                        _clockTick.value = System.currentTimeMillis()
                        showMessage(s.msgSyncSuccess(result.syncedYears, result.entriesCount, result.sourceName))
                    }
                    is HolidaySyncResult.Failure -> {
                        showMessage(
                            if (s.language == AppLanguage.EN) s.msgSyncFailedFallback
                            else result.message
                        )
                    }
                }
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun setManualHolidayOverride(date: SimpleDate, dayType: ChinaDayType, customLabel: String) {
        viewModelScope.launch {
            val s = currentStrings()
            holidayRepo.setManualOverride(date, dayType, customLabel)
            scheduler.rescheduleAll()
            _clockTick.value = System.currentTimeMillis()
            showMessage(s.msgManualOverrideSet(s.formatShortDate(date), s.dayTypeDisplayName(dayType)))
        }
    }

    fun setManualHolidayRange(
        startDate: SimpleDate,
        endDate: SimpleDate,
        dayType: ChinaDayType,
        customLabel: String
    ) {
        viewModelScope.launch {
            val s = currentStrings()
            val count = holidayRepo.setManualOverrideRange(startDate, endDate, dayType, customLabel)
            scheduler.rescheduleAll()
            _clockTick.value = System.currentTimeMillis()
            showMessage(s.msgManualRangeSet(count, s.dayTypeDisplayName(dayType)))
        }
    }

    fun revertManualHolidayOverride(date: SimpleDate) {
        viewModelScope.launch {
            val s = currentStrings()
            holidayRepo.revertManualOverride(date)
            scheduler.rescheduleAll()
            _clockTick.value = System.currentTimeMillis()
            showMessage(s.msgOverrideReverted(s.formatShortDate(date)))
        }
    }

    fun resetAllManualOverrides() {
        viewModelScope.launch {
            val s = currentStrings()
            holidayRepo.resetAllOverridesToOfficial()
            scheduler.rescheduleAll()
            _clockTick.value = System.currentTimeMillis()
            showMessage(s.msgAllOverridesReset)
        }
    }

    fun importManualScheduleText(rawText: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val s = currentStrings()
            val res = holidayRepo.importManualScheduleText(rawText)
            res.onSuccess { count ->
                scheduler.rescheduleAll()
                _clockTick.value = System.currentTimeMillis()
                showMessage(s.msgImportSuccess(count))
                onDone(true)
            }.onFailure { err ->
                showMessage(
                    if (s.language == AppLanguage.EN) s.msgImportFailed
                    else (err.message ?: s.msgImportFailed)
                )
                onDone(false)
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefsRepo.setThemeMode(mode) }
    }

    fun setThemePalette(palette: ThemePalette) {
        viewModelScope.launch { prefsRepo.setThemePalette(palette) }
    }

    fun setUseDynamicColor(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setUseDynamicColor(enabled) }
    }

    fun setAppLanguage(language: AppLanguage) {
        viewModelScope.launch { prefsRepo.setAppLanguage(language) }
    }

    fun setAutoSyncInterval(interval: AutoSyncInterval) {
        viewModelScope.launch { prefsRepo.setAutoSyncInterval(interval) }
    }

    fun setOneUiExpandedHeader(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setOneUiExpandedHeader(enabled) }
    }

    fun testRingAlarm(alarm: AlarmEntity) {
        val s = currentStrings()
        val intent = Intent(app, AlarmRingingService::class.java).apply {
            action = AlarmConstants.ACTION_TEST_RING
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmConstants.EXTRA_ALARM_LABEL, alarm.label.ifBlank { s.testRingSampleLabel })
            putExtra(AlarmConstants.EXTRA_ALARM_TIME, alarm.formattedTime())
            putExtra(
                AlarmConstants.EXTRA_DAY_BADGE,
                s.localizeHolidayLabel(uiState.value.todayInfo.label, uiState.value.todayInfo.dayType)
            )
            putExtra(AlarmConstants.EXTRA_SNOOZE_MINUTES, alarm.snoozeMinutes)
            putExtra(AlarmConstants.EXTRA_VIBRATE, alarm.vibrate)
            putExtra(AlarmConstants.EXTRA_GRADUAL_VOLUME, alarm.gradualVolume)
            putExtra(AlarmConstants.EXTRA_RINGTONE_URI, alarm.ringtoneUri)
        }
        ContextCompat.startForegroundService(app, intent)
    }

    fun canScheduleExactAlarms(): Boolean = scheduler.canScheduleExactAlarms()
}
