package com.example.domain

import com.example.data.model.AlarmEntity
import com.example.data.model.AlarmGroupEntity
import com.example.data.model.ChinaDayType
import com.example.data.model.HolidayDayEntity
import com.example.data.model.HolidayRuleMode
import com.example.data.model.ResolvedDayInfo
import com.example.data.model.SimpleDate
import java.util.Calendar

data class NextAlarmOccurrence(
    val alarm: AlarmEntity,
    val group: AlarmGroupEntity?,
    val triggerDate: SimpleDate,
    val triggerMillis: Long,
    val dayInfo: ResolvedDayInfo,
    val statusSummary: String,
    val skippedReason: String? = null
)

data class DayScheduleSummary(
    val dayInfo: ResolvedDayInfo,
    val activeAlarms: List<AlarmWithGroup>,
    val autoPausedAlarms: List<AlarmWithGroup>
)

data class AlarmWithGroup(
    val alarm: AlarmEntity,
    val group: AlarmGroupEntity?
)

/**
 * Pure deterministic scheduling engine for China Mainland statutory holidays & lieu workdays.
 * Calculates whether any alarm or alarm group should auto-enable or auto-skip on a given date,
 * and resolves the exact next trigger timestamp for Android AlarmManager.
 */
object AlarmScheduleCalculator {

    /**
     * Resolves a date against the holiday/lieu-workday lookup map.
     * Supports official State Council entries as well as user manual overrides.
     */
    fun resolveDay(
        date: SimpleDate,
        holidayMap: Map<String, HolidayDayEntity>
    ): ResolvedDayInfo {
        val iso = date.toIsoString()
        val entry = holidayMap[iso]

        if (entry != null) {
            if (entry.isUserOverride && !entry.overrideType.isNullOrBlank()) {
                val type = when (entry.overrideType) {
                    ChinaDayType.STATUTORY_HOLIDAY.name -> ChinaDayType.STATUTORY_HOLIDAY
                    ChinaDayType.LIEU_WORKDAY.name -> ChinaDayType.LIEU_WORKDAY
                    ChinaDayType.WORKDAY.name -> ChinaDayType.WORKDAY
                    ChinaDayType.WEEKEND.name -> ChinaDayType.WEEKEND
                    else -> if (entry.isOffDay) ChinaDayType.STATUTORY_HOLIDAY else ChinaDayType.LIEU_WORKDAY
                }
                return ResolvedDayInfo(
                    date = date,
                    dayType = type,
                    label = entry.name.ifBlank { type.displayName },
                    isUserOverride = true,
                    source = entry.source
                )
            }
            val type = if (entry.isOffDay) ChinaDayType.STATUTORY_HOLIDAY else ChinaDayType.LIEU_WORKDAY
            return ResolvedDayInfo(
                date = date,
                dayType = type,
                label = entry.name.ifBlank { type.displayName },
                isUserOverride = entry.isUserOverride,
                source = entry.source
            )
        }

        return if (date.isRegularWeekend) {
            ResolvedDayInfo(
                date = date,
                dayType = ChinaDayType.WEEKEND,
                label = "周末",
                isUserOverride = false,
                source = "calendar"
            )
        } else {
            ResolvedDayInfo(
                date = date,
                dayType = ChinaDayType.WORKDAY,
                label = "工作日",
                isUserOverride = false,
                source = "calendar"
            )
        }
    }

    /**
     * Determines whether an enabled alarm should ring on [date] according to its effective rule
     * (either inherited from its alarm group or overridden individually).
     */
    fun shouldRingOnDate(
        alarm: AlarmEntity,
        group: AlarmGroupEntity?,
        date: SimpleDate,
        holidayMap: Map<String, HolidayDayEntity>
    ): Boolean {
        if (!alarm.isEffectivelyEnabled(group)) return false
        val dateIso = date.toIsoString()
        if (alarm.skippedDateIso == dateIso) return false
        if (group?.skippedDateIso == dateIso) return false

        val dayInfo = resolveDay(date, holidayMap)
        val rule = alarm.effectiveRuleMode(group)

        return when (rule) {
            HolidayRuleMode.LEGAL_WORKDAY -> {
                // Rings on Mon-Fri non-holidays AND weekend lieu workdays ("班")
                dayInfo.isWorkingDay
            }
            HolidayRuleMode.LEGAL_HOLIDAY_AND_WEEKEND -> {
                // Rings on regular weekends AND statutory holidays ("休"), skips lieu workdays ("班")
                dayInfo.isOffDay
            }
            HolidayRuleMode.CUSTOM_SKIP_HOLIDAY -> {
                val bitIndex = date.dayOfWeekIso - 1 // 0=Mon..6=Sun
                val mask = alarm.effectiveCustomDaysMask(group)
                (mask and (1 shl bitIndex)) != 0
            }
            HolidayRuleMode.EVERY_DAY -> true
            HolidayRuleMode.ONCE -> true
        }
    }

    /**
     * Explains why an alarm is skipped on a specific date (for rich One UI status badges).
     */
    fun skipReasonForDate(
        alarm: AlarmEntity,
        group: AlarmGroupEntity?,
        date: SimpleDate,
        holidayMap: Map<String, HolidayDayEntity>
    ): String? {
        if (!alarm.isEnabled) return "闹钟已关闭"
        if (group != null && !group.isEnabled) return "所属组「${group.name}」已关闭"
        val dateIso = date.toIsoString()
        if (group?.skippedDateIso == dateIso) return "所属组已跳过本日"
        if (alarm.skippedDateIso == dateIso) return "已手动跳过本日"

        val dayInfo = resolveDay(date, holidayMap)
        val rule = alarm.effectiveRuleMode(group)
        if (shouldRingOnDate(alarm, group, date, holidayMap)) return null

        return when (rule) {
            HolidayRuleMode.LEGAL_WORKDAY -> when (dayInfo.dayType) {
                ChinaDayType.STATUTORY_HOLIDAY -> "法定节假日「${dayInfo.label}」自动关闭"
                ChinaDayType.WEEKEND -> "周末休息自动关闭"
                else -> "非工作日"
            }
            HolidayRuleMode.LEGAL_HOLIDAY_AND_WEEKEND -> when (dayInfo.dayType) {
                ChinaDayType.LIEU_WORKDAY -> "调休补班「${dayInfo.label}」自动关闭"
                ChinaDayType.WORKDAY -> "工作日自动关闭"
                else -> "非休假日"
            }
            HolidayRuleMode.CUSTOM_SKIP_HOLIDAY -> "非选定星期"
            else -> null
        }
    }

    /**
     * Computes the exact next occurrence for a single alarm starting after [nowMillis].
     * Scans up to 366 days ahead so even multi-day Spring Festival / National Day breaks resolve cleanly.
     */
    fun computeNextOccurrence(
        alarm: AlarmEntity,
        group: AlarmGroupEntity?,
        holidayMap: Map<String, HolidayDayEntity>,
        nowMillis: Long = System.currentTimeMillis()
    ): NextAlarmOccurrence? {
        if (!alarm.isEffectivelyEnabled(group)) return null

        val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val today = SimpleDate(
            year = nowCal.get(Calendar.YEAR),
            month = nowCal.get(Calendar.MONTH) + 1,
            day = nowCal.get(Calendar.DAY_OF_MONTH)
        )

        var firstSkippedNote: String? = null

        for (offset in 0..366) {
            val candidateDate = today.plusDays(offset)
            val triggerMillis = candidateDate.toTriggerMillis(alarm.hour, alarm.minute)
            if (triggerMillis <= nowMillis + 1_000L) continue

            if (shouldRingOnDate(alarm, group, candidateDate, holidayMap)) {
                val dayInfo = resolveDay(candidateDate, holidayMap)
                val relativePrefix = when (today.daysUntil(candidateDate)) {
                    0 -> "今天"
                    1 -> "明天"
                    2 -> "后天"
                    else -> candidateDate.toChineseShort()
                }
                val dayBadge = when (dayInfo.dayType) {
                    ChinaDayType.LIEU_WORKDAY -> "调休补班 · 自动开启"
                    ChinaDayType.STATUTORY_HOLIDAY -> "${dayInfo.label}(休)"
                    ChinaDayType.WEEKEND -> candidateDate.toChineseWeekday()
                    ChinaDayType.WORKDAY -> "${candidateDate.toChineseWeekday()} 工作日"
                }
                val summary = "$relativePrefix ${alarm.formattedTime()} · $dayBadge"
                return NextAlarmOccurrence(
                    alarm = alarm,
                    group = group,
                    triggerDate = candidateDate,
                    triggerMillis = triggerMillis,
                    dayInfo = dayInfo,
                    statusSummary = summary,
                    skippedReason = firstSkippedNote
                )
            } else if (firstSkippedNote == null && offset <= 2) {
                val candidateInfo = resolveDay(candidateDate, holidayMap)
                val iso = candidateDate.toIsoString()
                if (candidateInfo.dayType == ChinaDayType.STATUTORY_HOLIDAY ||
                    candidateInfo.dayType == ChinaDayType.LIEU_WORKDAY ||
                    alarm.skippedDateIso == iso ||
                    group?.skippedDateIso == iso
                ) {
                    firstSkippedNote = skipReasonForDate(alarm, group, candidateDate, holidayMap)
                }
            }
        }
        return null
    }

    /**
     * Generates a day-by-day preview of which alarms auto-enable and which auto-pause
     * over the next [daysCount] days.
     */
    fun buildSchedulePreview(
        alarms: List<AlarmEntity>,
        groupsById: Map<Long, AlarmGroupEntity>,
        holidayMap: Map<String, HolidayDayEntity>,
        startDate: SimpleDate = SimpleDate.today(),
        daysCount: Int = 14
    ): List<DayScheduleSummary> {
        return (0 until daysCount).map { offset ->
            val date = startDate.plusDays(offset)
            val dayInfo = resolveDay(date, holidayMap)
            val active = mutableListOf<AlarmWithGroup>()
            val paused = mutableListOf<AlarmWithGroup>()

            alarms.forEach { alarm ->
                val group = alarm.groupId?.let { groupsById[it] }
                if (alarm.isEffectivelyEnabled(group)) {
                    if (shouldRingOnDate(alarm, group, date, holidayMap)) {
                        active.add(AlarmWithGroup(alarm, group))
                    } else {
                        paused.add(AlarmWithGroup(alarm, group))
                    }
                }
            }
            DayScheduleSummary(
                dayInfo = dayInfo,
                activeAlarms = active,
                autoPausedAlarms = paused
            )
        }
    }

    fun formatCountdown(targetMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        val diffMinutes = ((targetMillis - nowMillis).coerceAtLeast(0L) + 59_999L) / 60_000L
        val days = diffMinutes / (24 * 60)
        val hours = (diffMinutes % (24 * 60)) / 60
        val mins = diffMinutes % 60
        return when {
            days > 0 && hours > 0 -> "${days}天 ${hours}小时后响铃"
            days > 0 -> "${days}天后响铃"
            hours > 0 && mins > 0 -> "${hours}小时 ${mins}分钟后响铃"
            hours > 0 -> "${hours}小时后响铃"
            mins <= 1 -> "不到 1 分钟后响铃"
            else -> "${mins}分钟后响铃"
        }
    }

    fun formatCustomDaysMask(mask: Int): String {
        val labels = listOf("一", "二", "三", "四", "五", "六", "日")
        val selected = labels.indices.filter { (mask and (1 shl it)) != 0 }
        return when {
            selected.isEmpty() -> "未选星期"
            selected.size == 7 -> "每天"
            selected == listOf(0, 1, 2, 3, 4) -> "周一至周五"
            selected == listOf(5, 6) -> "周六、周日"
            else -> "周" + selected.joinToString("、") { labels[it] }
        }
    }
}
