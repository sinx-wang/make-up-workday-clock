package com.example.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.data.local.AutoSyncInterval
import com.example.data.local.ThemeMode
import com.example.data.local.ThemePalette
import com.example.data.model.AlarmEntity
import com.example.data.model.AlarmGroupEntity
import com.example.data.model.ChinaDayType
import com.example.data.model.HolidayDayEntity
import com.example.data.model.HolidayRuleMode
import com.example.data.model.SimpleDate
import com.example.domain.AlarmScheduleCalculator
import java.util.Locale

interface AppStrings {
    val language: AppLanguage

    // App & Navigation
    val appTitle: String
    val navAlarms: String
    val navCalendar: String
    val navSettings: String

    // Common Actions
    val cancel: String
    val confirm: String
    val edit: String
    val change: String
    val none: String
    val todayWord: String
    val tomorrowWord: String
    val dayAfterTomorrowWord: String
    val manualTag: String

    // Alarms Screen
    val syncHolidays: String
    val syncing: String
    val noActiveAlarms: String
    val exactAlarmWarning: String
    val enableNow: String
    val alarmGroupsHeader: String
    val newGroup: String
    val ungroupedAlarmsHeader: String
    val noAlarmsYet: String
    val newAlarm: String
    val editAlarm: String
    val deleteAlarm: String
    val addAlarm: String
    val collapseGroup: String
    val expandGroup: String
    fun expandGroupCount(count: Int): String
    val groupDisabled: String
    val noPendingAlarms: String
    val noSchedule: String
    fun skippedUndoLabel(dateLabel: String): String
    fun todayPendingCount(count: Int): String
    fun todayLieuPendingCount(count: Int): String
    fun tomorrowPendingCount(count: Int): String
    fun tomorrowLieuPendingCount(count: Int): String
    fun tomorrowHolidaySkipped(holidayName: String): String
    val tomorrowAutoSkipped: String
    fun tomorrowMixedStatus(active: Int, paused: Int): String
    val skipNext: String
    val editGroupDesc: String
    val independentPrefix: String
    val lieuWorkdayTag: String
    val afterHolidaySuffix: String
    fun heroTomorrowPrefix(label: String): String
    val defaultRingtoneTitle: String
    val customRingtoneTitle: String
    val defaultAlarmLabel: String
    val defaultGroupLabel: String

    // Alarm Editor Sheet
    val hourUnit: String
    val minuteUnit: String
    fun increaseUnit(unit: String): String
    fun decreaseUnit(unit: String): String
    val errorHourRange: String
    val errorMinuteRange: String
    val errorHourSaveRange: String
    val errorMinuteSaveRange: String
    val alarmNameLabel: String
    val alarmNamePlaceholder: String
    val alarmGroupSection: String
    val standaloneAlarm: String
    fun followGroupRule(groupName: String, ruleShort: String): String
    val repeatAndHolidayRules: String
    val alarmRingtone: String
    val selectRingtoneTitle: String
    val gradualVolume: String
    val vibrateReminder: String
    val snoozeInterval: String
    fun minutesLabel(minutes: Int): String
    val customOption: String
    val testRing: String
    val syncSystemClock: String
    val saveAndSchedule: String
    val customSnoozeTitle: String
    val customSnoozeHint: String
    val minuteUnitSuffix: String
    val customSnoozeErrorRange: String
    val customSnoozeErrorSubmit: String

    // Group Editor Sheet
    val newGroupTitle: String
    val editGroupTitle: String
    val deleteGroupDesc: String
    val groupNameLabel: String
    val groupNamePlaceholder: String
    val iconAndAccent: String
    val selectedColorDesc: String
    val saveGroup: String
    fun groupIconLabel(key: String): String

    // Calendar Screen
    val holidaySyncTitle: String
    val builtinBaseline: String
    fun updatedAt(timeText: String): String
    val onlineUpdate: String
    val manualImport: String
    fun yearChip(year: Int): String
    val jumpToToday: String
    val prevMonth: String
    val nextMonth: String
    val weekdayInitials: List<String>
    val calendarBadgeHoliday: String
    val calendarBadgeLieu: String
    val calendarBadgeOverride: String
    val calendarBadgeToday: String
    val legendHoliday: String
    val legendLieu: String
    fun yearScheduleHeader(year: Int): String
    val noRecords: String
    val adjustDay: String
    fun activeAlarmsCount(count: Int): String
    fun inactiveAlarmsCount(count: Int): String

    // Holiday Override & Batch Import Sheets
    val noteLabel: String
    val notePlaceholder: String
    val applyMultiDayTitle: String
    val onlyThisDay: String
    fun spanDaysChip(count: Int): String
    val revertOfficialDefault: String
    val saveAndReschedule: String
    val manualImportTitle: String
    val manualImportTemplate: String
    val parseAndUpdate: String

    // Settings Screen
    val permissionsTitle: String
    val permissionsReady: String
    val permExactAlarm: String
    val permBatteryUnrestricted: String
    val permNotifications: String
    val permFullScreen: String
    val permGranted: String
    val permEnable: String
    val appearanceTitle: String
    val themeModeTitle: String
    val themePaletteTitle: String
    val followWallpaper: String
    val dynamicColorTitle: String
    val expandedHeaderTitle: String
    val languageTitle: String
    val languageDialogTitle: String
    val followSystemLanguage: String
    val autoSyncTitle: String
    fun resetOverridesButton(count: Int): String
    val testRingScreenButton: String
    val testRingSampleLabel: String

    // Ringing Activity & Notification
    val ringingDefaultBadge: String
    fun ringingTestStatus(badge: String): String
    fun ringingTodayStatus(badge: String): String
    val endTestPreview: String
    fun snoozeAfterMinutes(minutes: Int): String
    val dismissAlarm: String
    fun notifTodayBody(badge: String): String
    val notifDefaultBody: String
    fun notifSnoozeAction(minutes: Int): String

    // Enums & Formatters
    fun themeModeLabel(mode: ThemeMode): String
    fun themePaletteName(palette: ThemePalette): String
    fun autoSyncLabel(interval: AutoSyncInterval): String
    fun languageOptionLabel(lang: AppLanguage): String
    fun dayTypeDisplayName(type: ChinaDayType): String
    fun dayTypeShortBadge(type: ChinaDayType): String
    fun dayTypeDefaultPill(type: ChinaDayType): String
    fun ruleModeTitle(mode: HolidayRuleMode): String
    fun ruleModeShortTag(mode: HolidayRuleMode): String
    fun ruleModeDescription(mode: HolidayRuleMode): String

    fun formatShortDate(date: SimpleDate): String
    fun formatFullDate(date: SimpleDate): String
    fun formatYearMonth(year: Int, month: Int): String
    fun formatWeekdayShort(date: SimpleDate): String
    fun formatRelativeDay(today: SimpleDate, target: SimpleDate): String
    fun formatSkippedDateWord(today: SimpleDate, target: SimpleDate): String
    fun formatCountdown(targetMillis: Long, nowMillis: Long): String
    fun formatCustomDaysMask(mask: Int): String
    fun localizeHolidayLabel(rawLabel: String, dayType: ChinaDayType): String
    fun skipReasonForDate(
        alarm: AlarmEntity,
        group: AlarmGroupEntity?,
        date: SimpleDate,
        holidayMap: Map<String, HolidayDayEntity>
    ): String?

    // Snackbars / Feedback Messages
    val msgGroupEnabled: String
    val msgGroupDisabled: String
    fun msgGroupSkipRestored(groupName: String): String
    fun msgGroupSkipped(groupName: String, dateShort: String): String
    val msgAlarmSkipRestored: String
    fun msgAlarmSkipped(dateShort: String): String
    val msgAlarmSaved: String
    fun msgAlarmDeleted(name: String): String
    fun msgGroupSaved(name: String): String
    val msgGroupDeleted: String
    fun msgSyncSuccess(years: List<Int>, count: Int, source: String): String
    val msgSyncFailedFallback: String
    fun msgManualOverrideSet(dateShort: String, typeName: String): String
    fun msgManualRangeSet(count: Int, typeName: String): String
    fun msgOverrideReverted(dateShort: String): String
    val msgAllOverridesReset: String
    fun msgImportSuccess(count: Int): String
    val msgImportFailed: String
}

object ZhCnStrings : AppStrings {
    override val language = AppLanguage.ZH_CN

    override val appTitle = "节律闹钟"
    override val navAlarms = "闹钟"
    override val navCalendar = "日历"
    override val navSettings = "设置"

    override val cancel = "取消"
    override val confirm = "确定"
    override val edit = "编辑"
    override val change = "更改"
    override val none = "无"
    override val todayWord = "今天"
    override val tomorrowWord = "明天"
    override val dayAfterTomorrowWord = "后天"
    override val manualTag = "手动"

    override val syncHolidays = "同步假期"
    override val syncing = "同步中…"
    override val noActiveAlarms = "暂无待响铃闹钟"
    override val exactAlarmWarning = "请开启精确闹钟权限以确保准点响铃"
    override val enableNow = "去开启"
    override val alarmGroupsHeader = "闹钟组"
    override val newGroup = "新建组"
    override val ungroupedAlarmsHeader = "独立闹钟"
    override val noAlarmsYet = "暂无闹钟"
    override val newAlarm = "新建闹钟"
    override val editAlarm = "编辑闹钟"
    override val deleteAlarm = "删除闹钟"
    override val addAlarm = "添加闹钟"
    override val collapseGroup = "收起闹钟组"
    override val expandGroup = "展开闹钟组"
    override fun expandGroupCount(count: Int) = "展开（$count）"
    override val groupDisabled = "已关闭"
    override val noPendingAlarms = "暂无待响闹钟"
    override val noSchedule = "暂无排期"
    override fun skippedUndoLabel(dateLabel: String) = "已跳过$dateLabel · 撤销"
    override fun todayPendingCount(count: Int) = "今日待响 $count 个"
    override fun todayLieuPendingCount(count: Int) = "今日调休 · 待响 $count 个"
    override fun tomorrowPendingCount(count: Int) = "明日待响 $count 个"
    override fun tomorrowLieuPendingCount(count: Int) = "明日调休 · 开启 $count 个"
    override fun tomorrowHolidaySkipped(holidayName: String) = "明日「$holidayName」自动跳过"
    override val tomorrowAutoSkipped = "明日自动跳过"
    override fun tomorrowMixedStatus(active: Int, paused: Int) = "明日 $active 开 · $paused 休"
    override val skipNext = "跳过下次"
    override val editGroupDesc = "编辑闹钟组"
    override val independentPrefix = "独立 · "
    override val lieuWorkdayTag = "调休补班"
    override val afterHolidaySuffix = "（假期后）"
    override fun heroTomorrowPrefix(label: String) = "明日 $label"
    override val defaultRingtoneTitle = "系统默认铃声"
    override val customRingtoneTitle = "自定义闹钟铃声"
    override val defaultAlarmLabel = "智能闹钟"
    override val defaultGroupLabel = "智能闹钟组"

    override val hourUnit = "时"
    override val minuteUnit = "分"
    override fun increaseUnit(unit: String) = "增加$unit"
    override fun decreaseUnit(unit: String) = "减少$unit"
    override val errorHourRange = "小时范围 00–23"
    override val errorMinuteRange = "分钟范围 00–59"
    override val errorHourSaveRange = "请输入 00–23 之间的小时"
    override val errorMinuteSaveRange = "请输入 00–59 之间的分钟"
    override val alarmNameLabel = "闹钟名称"
    override val alarmNamePlaceholder = "例如：早起通勤、送孩子上学、假日早餐"
    override val alarmGroupSection = "所属闹钟组"
    override val standaloneAlarm = "独立闹钟"
    override fun followGroupRule(groupName: String, ruleShort: String) = "跟随「$groupName」规则（$ruleShort）"
    override val repeatAndHolidayRules = "重复与节假日规则"
    override val alarmRingtone = "闹钟铃声"
    override val selectRingtoneTitle = "选择闹钟铃声"
    override val gradualVolume = "渐强音量"
    override val vibrateReminder = "同步震动提醒"
    override val snoozeInterval = "稍后提醒间隔"
    override fun minutesLabel(minutes: Int) = "${minutes}分钟"
    override val customOption = "自定义"
    override val testRing = "试听响铃"
    override val syncSystemClock = "同步系统时钟"
    override val saveAndSchedule = "保存并智能排期"
    override val customSnoozeTitle = "自定义稍后提醒间隔"
    override val customSnoozeHint = "间隔时长（1–60）"
    override val minuteUnitSuffix = "分钟"
    override val customSnoozeErrorRange = "范围 1–60 分钟"
    override val customSnoozeErrorSubmit = "请输入 1–60 之间的整数"

    override val newGroupTitle = "新建闹钟组"
    override val editGroupTitle = "编辑闹钟组"
    override val deleteGroupDesc = "删除闹钟组"
    override val groupNameLabel = "闹钟组名称"
    override val groupNamePlaceholder = "例如：早起通勤组、孩子上学组"
    override val iconAndAccent = "图标与主题强调色"
    override val selectedColorDesc = "已选颜色"
    override val saveGroup = "保存闹钟组"
    override fun groupIconLabel(key: String) = when (key) {
        "work" -> "通勤"
        "holiday" -> "休假"
        "fitness" -> "运动"
        "family" -> "家庭"
        "study" -> "学习"
        else -> "自定义"
    }

    override val holidaySyncTitle = "假期同步"
    override val builtinBaseline = "内置基准"
    override fun updatedAt(timeText: String) = "更新于 $timeText"
    override val onlineUpdate = "在线更新"
    override val manualImport = "手动导入"
    override fun yearChip(year: Int) = "${year}年"
    override val jumpToToday = "回到今天"
    override val prevMonth = "上个月"
    override val nextMonth = "下个月"
    override val weekdayInitials = listOf("一", "二", "三", "四", "五", "六", "日")
    override val calendarBadgeHoliday = "休"
    override val calendarBadgeLieu = "班"
    override val calendarBadgeOverride = "调"
    override val calendarBadgeToday = "今"
    override val legendHoliday = "放假"
    override val legendLieu = "补班"
    override fun yearScheduleHeader(year: Int) = "${year}年 放假与调休"
    override val noRecords = "暂无记录"
    override val adjustDay = "调整当日"
    override fun activeAlarmsCount(count: Int) = "生效闹钟（$count）"
    override fun inactiveAlarmsCount(count: Int) = "未生效闹钟（$count）"

    override val noteLabel = "备注名称"
    override val notePlaceholder = "例如：年假、公司调休、国庆加班"
    override val applyMultiDayTitle = "连续应用天数（批量设置连休/连班）"
    override val onlyThisDay = "仅当天"
    override fun spanDaysChip(count: Int) = "连设${count}天"
    override val revertOfficialDefault = "恢复官方默认安排"
    override val saveAndReschedule = "保存并重排闹钟"
    override val manualImportTitle = "手动导入节假日"
    override val manualImportTemplate = """
        # 示例：每行「日期 休/班 名称」，或粘贴 holiday-cn JSON
        2027-01-01 休 元旦
        2027-02-14 班 春节调休补班
    """.trimIndent()
    override val parseAndUpdate = "立即解析并更新"

    override val permissionsTitle = "必要权限"
    override val permissionsReady = "已就绪"
    override val permExactAlarm = "精确闹钟"
    override val permBatteryUnrestricted = "电池不受限制"
    override val permNotifications = "通知提醒"
    override val permFullScreen = "锁屏全屏响铃"
    override val permGranted = "已开启"
    override val permEnable = "去开启"
    override val appearanceTitle = "外观设置"
    override val themeModeTitle = "深浅模式"
    override val themePaletteTitle = "主题调色板"
    override val followWallpaper = "跟随壁纸"
    override val dynamicColorTitle = "壁纸动态取色"
    override val expandedHeaderTitle = "单手大视区顶栏"
    override val languageTitle = "语言"
    override val languageDialogTitle = "选择语言"
    override val followSystemLanguage = "跟随系统"
    override val autoSyncTitle = "假期自动更新"
    override fun resetOverridesButton(count: Int) = "重置 $count 项手动修改"
    override val testRingScreenButton = "测试响铃界面"
    override val testRingSampleLabel = "响铃测试"

    override val ringingDefaultBadge = "智能工作日"
    override fun ringingTestStatus(badge: String) = "测试响铃预览 · $badge"
    override fun ringingTodayStatus(badge: String) = "今日状态：$badge"
    override val endTestPreview = "结束测试预览"
    override fun snoozeAfterMinutes(minutes: Int) = "稍后提醒（$minutes 分钟后）"
    override val dismissAlarm = "关闭闹钟"
    override fun notifTodayBody(badge: String) = "今日：$badge"
    override val notifDefaultBody = "法定节假日智能闹钟正在响铃"
    override fun notifSnoozeAction(minutes: Int) = "稍后 $minutes 分钟"

    override fun themeModeLabel(mode: ThemeMode) = when (mode) {
        ThemeMode.SYSTEM -> "跟随系统"
        ThemeMode.DARK -> "深色"
        ThemeMode.LIGHT -> "浅色"
    }

    override fun themePaletteName(palette: ThemePalette) = when (palette) {
        ThemePalette.COBALT -> "钴蓝"
        ThemePalette.EMERALD -> "翡翠"
        ThemePalette.VIOLET -> "星紫"
        ThemePalette.CORAL -> "珊瑚"
        ThemePalette.AMBER -> "琥珀"
        ThemePalette.CYAN -> "冰青"
        ThemePalette.ROSE -> "樱粉"
    }

    override fun autoSyncLabel(interval: AutoSyncInterval) = when (interval) {
        AutoSyncInterval.DAILY -> "每天"
        AutoSyncInterval.WEEKLY -> "每周"
        AutoSyncInterval.MANUAL -> "仅手动"
    }

    override fun languageOptionLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.SYSTEM -> followSystemLanguage
        else -> lang.nativeName
    }

    override fun dayTypeDisplayName(type: ChinaDayType) = type.displayName
    override fun dayTypeShortBadge(type: ChinaDayType) = type.shortBadge
    override fun dayTypeDefaultPill(type: ChinaDayType) = when (type) {
        ChinaDayType.STATUTORY_HOLIDAY -> "法定休"
        ChinaDayType.LIEU_WORKDAY -> "调休补班"
        ChinaDayType.WORKDAY -> "工作日"
        ChinaDayType.WEEKEND -> "周末"
    }

    override fun ruleModeTitle(mode: HolidayRuleMode) = mode.title
    override fun ruleModeShortTag(mode: HolidayRuleMode) = mode.shortTag
    override fun ruleModeDescription(mode: HolidayRuleMode) = mode.description

    override fun formatShortDate(date: SimpleDate) = date.toChineseShort()
    override fun formatFullDate(date: SimpleDate) = "${date.year}年${date.toChineseShort()} ${date.toChineseWeekday()}"
    override fun formatYearMonth(year: Int, month: Int) = "${year}年 ${month}月"
    override fun formatWeekdayShort(date: SimpleDate) = date.toChineseWeekday()

    override fun formatRelativeDay(today: SimpleDate, target: SimpleDate): String {
        return when (today.daysUntil(target)) {
            0 -> todayWord
            1 -> tomorrowWord
            2 -> dayAfterTomorrowWord
            in 3..6 -> target.toChineseWeekday()
            else -> target.toChineseShort()
        }
    }

    override fun formatSkippedDateWord(today: SimpleDate, target: SimpleDate): String {
        return when (today.daysUntil(target)) {
            0 -> "今日"
            1 -> "明日"
            else -> target.toChineseShort()
        }
    }

    override fun formatCountdown(targetMillis: Long, nowMillis: Long): String =
        AlarmScheduleCalculator.formatCountdown(targetMillis, nowMillis)

    override fun formatCustomDaysMask(mask: Int): String =
        AlarmScheduleCalculator.formatCustomDaysMask(mask)

    override fun localizeHolidayLabel(rawLabel: String, dayType: ChinaDayType): String =
        rawLabel.ifBlank { dayTypeDisplayName(dayType) }

    override fun skipReasonForDate(
        alarm: AlarmEntity,
        group: AlarmGroupEntity?,
        date: SimpleDate,
        holidayMap: Map<String, HolidayDayEntity>
    ): String? = AlarmScheduleCalculator.skipReasonForDate(alarm, group, date, holidayMap)

    override val msgGroupEnabled = "已开启闹钟组并重新排期"
    override val msgGroupDisabled = "已关闭整个闹钟组"
    override fun msgGroupSkipRestored(groupName: String) = "已恢复「$groupName」的下一次响铃"
    override fun msgGroupSkipped(groupName: String, dateShort: String) = "已跳过「$groupName」$dateShort 的全部响铃"
    override val msgAlarmSkipRestored = "已恢复下一次响铃"
    override fun msgAlarmSkipped(dateShort: String) = "已跳过 $dateShort 的响铃"
    override val msgAlarmSaved = "闹钟已保存并完成节假日智能排期"
    override fun msgAlarmDeleted(name: String) = "已删除闹钟「$name」"
    override fun msgGroupSaved(name: String) = "闹钟组「$name」规则已生效"
    override val msgGroupDeleted = "已删除闹钟组（组内闹钟已转为独立闹钟）"
    override fun msgSyncSuccess(years: List<Int>, count: Int, source: String) =
        "同步成功：已更新 ${years.joinToString("/")} 年（$count 条，来源：$source）"
    override val msgSyncFailedFallback = "网络连接失败，当前已自动保持内置及本地缓存假期安排"
    override fun msgManualOverrideSet(dateShort: String, typeName: String) =
        "已手动将 $dateShort 设为「$typeName」，闹钟已自动重排"
    override fun msgManualRangeSet(count: Int, typeName: String) =
        "已批量设置 $count 天为「$typeName」，闹钟已自动重排"
    override fun msgOverrideReverted(dateShort: String) = "已恢复 $dateShort 为官方默认安排"
    override val msgAllOverridesReset = "已清除所有手动覆盖并恢复官方默认"
    override fun msgImportSuccess(count: Int) = "成功导入 $count 条节假日/调休记录并重排闹钟"
    override val msgImportFailed = "导入失败，请检查格式"
}

object EnStrings : AppStrings {
    override val language = AppLanguage.EN

    private val shortMonths = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    private val shortWeekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    override val appTitle = "Rhythm Alarm"
    override val navAlarms = "Alarms"
    override val navCalendar = "Calendar"
    override val navSettings = "Settings"

    override val cancel = "Cancel"
    override val confirm = "Confirm"
    override val edit = "Edit"
    override val change = "Change"
    override val none = "None"
    override val todayWord = "Today"
    override val tomorrowWord = "Tomorrow"
    override val dayAfterTomorrowWord = "In 2 days"
    override val manualTag = "Custom"

    override val syncHolidays = "Sync"
    override val syncing = "Syncing…"
    override val noActiveAlarms = "No upcoming alarms"
    override val exactAlarmWarning = "Allow exact alarms for on-time ringing"
    override val enableNow = "Enable"
    override val alarmGroupsHeader = "Alarm Groups"
    override val newGroup = "New group"
    override val ungroupedAlarmsHeader = "Standalone Alarms"
    override val noAlarmsYet = "No alarms yet"
    override val newAlarm = "New Alarm"
    override val editAlarm = "Edit Alarm"
    override val deleteAlarm = "Delete alarm"
    override val addAlarm = "Add alarm"
    override val collapseGroup = "Collapse group"
    override val expandGroup = "Expand group"
    override fun expandGroupCount(count: Int) = "Expand ($count)"
    override val groupDisabled = "Off"
    override val noPendingAlarms = "No upcoming alarms"
    override val noSchedule = "Not scheduled"
    override fun skippedUndoLabel(dateLabel: String) = "Skipped $dateLabel · Undo"
    override fun todayPendingCount(count: Int) =
        if (count == 1) "1 left today" else "$count left today"
    override fun todayLieuPendingCount(count: Int) =
        if (count == 1) "Lieu workday · 1 left today" else "Lieu workday · $count left today"
    override fun tomorrowPendingCount(count: Int) =
        if (count == 1) "1 active tomorrow" else "$count active tomorrow"
    override fun tomorrowLieuPendingCount(count: Int) =
        if (count == 1) "Lieu tomorrow · 1 active" else "Lieu tomorrow · $count active"
    override fun tomorrowHolidaySkipped(holidayName: String) = "Auto-skipped tomorrow ($holidayName)"
    override val tomorrowAutoSkipped = "Auto-skipped tomorrow"
    override fun tomorrowMixedStatus(active: Int, paused: Int) = "Tomorrow: $active on · $paused off"
    override val skipNext = "Skip next"
    override val editGroupDesc = "Edit group"
    override val independentPrefix = "Custom · "
    override val lieuWorkdayTag = "Lieu workday"
    override val afterHolidaySuffix = " (after holiday)"
    override fun heroTomorrowPrefix(label: String) = "Tomorrow: $label"
    override val defaultRingtoneTitle = "System default"
    override val customRingtoneTitle = "Custom ringtone"
    override val defaultAlarmLabel = "Smart Alarm"
    override val defaultGroupLabel = "Smart Group"

    override val hourUnit = "Hr"
    override val minuteUnit = "Min"
    override fun increaseUnit(unit: String) = "Increase $unit"
    override fun decreaseUnit(unit: String) = "Decrease $unit"
    override val errorHourRange = "Hour must be 00–23"
    override val errorMinuteRange = "Minute must be 00–59"
    override val errorHourSaveRange = "Enter an hour between 00 and 23"
    override val errorMinuteSaveRange = "Enter a minute between 00 and 59"
    override val alarmNameLabel = "Alarm name"
    override val alarmNamePlaceholder = "e.g., Morning commute, School drop-off"
    override val alarmGroupSection = "Alarm group"
    override val standaloneAlarm = "Standalone"
    override fun followGroupRule(groupName: String, ruleShort: String) = "Follow \"$groupName\" ($ruleShort)"
    override val repeatAndHolidayRules = "Repeat & holiday rule"
    override val alarmRingtone = "Ringtone"
    override val selectRingtoneTitle = "Choose alarm ringtone"
    override val gradualVolume = "Gradual volume"
    override val vibrateReminder = "Vibrate"
    override val snoozeInterval = "Snooze interval"
    override fun minutesLabel(minutes: Int) = "${minutes} min"
    override val customOption = "Custom"
    override val testRing = "Test ring"
    override val syncSystemClock = "System Clock"
    override val saveAndSchedule = "Save & Schedule"
    override val customSnoozeTitle = "Custom snooze interval"
    override val customSnoozeHint = "Duration (1–60)"
    override val minuteUnitSuffix = "min"
    override val customSnoozeErrorRange = "Range: 1–60 min"
    override val customSnoozeErrorSubmit = "Enter a whole number from 1 to 60"

    override val newGroupTitle = "New Alarm Group"
    override val editGroupTitle = "Edit Alarm Group"
    override val deleteGroupDesc = "Delete group"
    override val groupNameLabel = "Group name"
    override val groupNamePlaceholder = "e.g., Workday Commute, Weekend"
    override val iconAndAccent = "Icon & accent color"
    override val selectedColorDesc = "Selected color"
    override val saveGroup = "Save Group"
    override fun groupIconLabel(key: String) = when (key) {
        "work" -> "Work"
        "holiday" -> "Holiday"
        "fitness" -> "Fitness"
        "family" -> "Family"
        "study" -> "Study"
        else -> "Custom"
    }

    override val holidaySyncTitle = "Holiday Sync"
    override val builtinBaseline = "Built-in"
    override fun updatedAt(timeText: String) = "Updated $timeText"
    override val onlineUpdate = "Sync online"
    override val manualImport = "Import"
    override fun yearChip(year: Int) = "$year"
    override val jumpToToday = "Jump to today"
    override val prevMonth = "Previous month"
    override val nextMonth = "Next month"
    override val weekdayInitials = listOf("M", "T", "W", "T", "F", "S", "S")
    override val calendarBadgeHoliday = "Off"
    override val calendarBadgeLieu = "Wrk"
    override val calendarBadgeOverride = "Mod"
    override val calendarBadgeToday = "Now"
    override val legendHoliday = "Holiday"
    override val legendLieu = "Lieu workday"
    override fun yearScheduleHeader(year: Int) = "$year Holidays & Lieu Days"
    override val noRecords = "No entries"
    override val adjustDay = "Edit day"
    override fun activeAlarmsCount(count: Int) = "Active alarms ($count)"
    override fun inactiveAlarmsCount(count: Int) = "Inactive alarms ($count)"

    override val noteLabel = "Label"
    override val notePlaceholder = "e.g., Annual leave, Company day off"
    override val applyMultiDayTitle = "Apply to consecutive days"
    override val onlyThisDay = "1 day"
    override fun spanDaysChip(count: Int) = "$count days"
    override val revertOfficialDefault = "Restore official schedule"
    override val saveAndReschedule = "Save & Reschedule"
    override val manualImportTitle = "Import Holidays"
    override val manualImportTemplate = """
        # Format: YYYY-MM-DD off/work Name, or paste holiday-cn JSON
        2027-01-01 休 New Year's Day
        2027-02-14 班 Spring Festival Lieu
    """.trimIndent()
    override val parseAndUpdate = "Parse & Update"

    override val permissionsTitle = "Required Permissions"
    override val permissionsReady = "Ready"
    override val permExactAlarm = "Exact alarms"
    override val permBatteryUnrestricted = "Unrestricted battery"
    override val permNotifications = "Notifications"
    override val permFullScreen = "Full-screen lockscreen alert"
    override val permGranted = "Active"
    override val permEnable = "Enable"
    override val appearanceTitle = "Appearance"
    override val themeModeTitle = "Theme mode"
    override val themePaletteTitle = "Color palette"
    override val followWallpaper = "Wallpaper"
    override val dynamicColorTitle = "Dynamic wallpaper color"
    override val expandedHeaderTitle = "One-handed expanded header"
    override val languageTitle = "Language"
    override val languageDialogTitle = "Choose language"
    override val followSystemLanguage = "Follow system"
    override val autoSyncTitle = "Auto Holiday Sync"
    override fun resetOverridesButton(count: Int) = "Reset $count custom overrides"
    override val testRingScreenButton = "Test Ringing Screen"
    override val testRingSampleLabel = "Ring Test"

    override val ringingDefaultBadge = "Smart Workday"
    override fun ringingTestStatus(badge: String) = "Test Preview · $badge"
    override fun ringingTodayStatus(badge: String) = "Today: $badge"
    override val endTestPreview = "End Preview"
    override fun snoozeAfterMinutes(minutes: Int) = "Snooze ($minutes min)"
    override val dismissAlarm = "Dismiss"
    override fun notifTodayBody(badge: String) = "Today: $badge"
    override val notifDefaultBody = "Holiday smart alarm is ringing"
    override fun notifSnoozeAction(minutes: Int) = "Snooze ${minutes}m"

    override fun themeModeLabel(mode: ThemeMode) = when (mode) {
        ThemeMode.SYSTEM -> "System"
        ThemeMode.DARK -> "Dark"
        ThemeMode.LIGHT -> "Light"
    }

    override fun themePaletteName(palette: ThemePalette) = when (palette) {
        ThemePalette.COBALT -> "Cobalt"
        ThemePalette.EMERALD -> "Emerald"
        ThemePalette.VIOLET -> "Violet"
        ThemePalette.CORAL -> "Coral"
        ThemePalette.AMBER -> "Amber"
        ThemePalette.CYAN -> "Cyan"
        ThemePalette.ROSE -> "Rose"
    }

    override fun autoSyncLabel(interval: AutoSyncInterval) = when (interval) {
        AutoSyncInterval.DAILY -> "Daily"
        AutoSyncInterval.WEEKLY -> "Weekly"
        AutoSyncInterval.MANUAL -> "Manual"
    }

    override fun languageOptionLabel(lang: AppLanguage) = when (lang) {
        AppLanguage.SYSTEM -> followSystemLanguage
        else -> lang.nativeName
    }

    override fun dayTypeDisplayName(type: ChinaDayType) = when (type) {
        ChinaDayType.WORKDAY -> "Workday"
        ChinaDayType.WEEKEND -> "Weekend"
        ChinaDayType.STATUTORY_HOLIDAY -> "Public Holiday"
        ChinaDayType.LIEU_WORKDAY -> "Lieu Workday"
    }

    override fun dayTypeShortBadge(type: ChinaDayType) = when (type) {
        ChinaDayType.WORKDAY, ChinaDayType.LIEU_WORKDAY -> "Wrk"
        ChinaDayType.WEEKEND, ChinaDayType.STATUTORY_HOLIDAY -> "Off"
    }

    override fun dayTypeDefaultPill(type: ChinaDayType) = dayTypeDisplayName(type)

    override fun ruleModeTitle(mode: HolidayRuleMode) = when (mode) {
        HolidayRuleMode.LEGAL_WORKDAY -> "Smart Workdays (incl. lieu days)"
        HolidayRuleMode.LEGAL_HOLIDAY_AND_WEEKEND -> "Holidays & Weekends Only"
        HolidayRuleMode.CUSTOM_SKIP_HOLIDAY -> "Custom Weekdays"
        HolidayRuleMode.EVERY_DAY -> "Every Day"
        HolidayRuleMode.ONCE -> "Ring Once"
    }

    override fun ruleModeShortTag(mode: HolidayRuleMode) = when (mode) {
        HolidayRuleMode.LEGAL_WORKDAY -> "Workdays"
        HolidayRuleMode.LEGAL_HOLIDAY_AND_WEEKEND -> "Holidays"
        HolidayRuleMode.CUSTOM_SKIP_HOLIDAY -> "Custom"
        HolidayRuleMode.EVERY_DAY -> "Daily"
        HolidayRuleMode.ONCE -> "Once"
    }

    override fun ruleModeDescription(mode: HolidayRuleMode) = when (mode) {
        HolidayRuleMode.LEGAL_WORKDAY -> "Auto-pauses on statutory holidays, rings on lieu workdays"
        HolidayRuleMode.LEGAL_HOLIDAY_AND_WEEKEND -> "Rings on holidays and weekends, pauses on lieu workdays"
        HolidayRuleMode.CUSTOM_SKIP_HOLIDAY -> "Rings on selected weekdays"
        HolidayRuleMode.EVERY_DAY -> "Rings every day regardless of holidays"
        HolidayRuleMode.ONCE -> "Rings once at the next occurrence and turns off"
    }

    override fun formatShortDate(date: SimpleDate): String {
        val m = shortMonths.getOrElse(date.month - 1) { "${date.month}" }
        return "$m ${date.day}"
    }

    override fun formatFullDate(date: SimpleDate): String {
        return "${formatWeekdayShort(date)}, ${formatShortDate(date)}, ${date.year}"
    }

    override fun formatYearMonth(year: Int, month: Int): String {
        val m = shortMonths.getOrElse(month - 1) { "$month" }
        return "$m $year"
    }

    override fun formatWeekdayShort(date: SimpleDate): String =
        shortWeekdays.getOrElse(date.dayOfWeekIso - 1) { "" }

    override fun formatRelativeDay(today: SimpleDate, target: SimpleDate): String {
        return when (today.daysUntil(target)) {
            0 -> todayWord
            1 -> tomorrowWord
            in 2..6 -> formatWeekdayShort(target)
            else -> formatShortDate(target)
        }
    }

    override fun formatSkippedDateWord(today: SimpleDate, target: SimpleDate): String {
        return when (today.daysUntil(target)) {
            0 -> "today"
            1 -> "tomorrow"
            else -> formatShortDate(target)
        }
    }

    override fun formatCountdown(targetMillis: Long, nowMillis: Long): String {
        val diffMinutes = ((targetMillis - nowMillis).coerceAtLeast(0L) + 59_999L) / 60_000L
        val days = diffMinutes / (24 * 60)
        val hours = (diffMinutes % (24 * 60)) / 60
        val mins = diffMinutes % 60
        return when {
            days > 0 && hours > 0 -> "Rings in ${days}d ${hours}h"
            days > 0 -> "Rings in ${days}d"
            hours > 0 && mins > 0 -> "Rings in ${hours}h ${mins}m"
            hours > 0 -> "Rings in ${hours}h"
            mins <= 1 -> "Rings in < 1m"
            else -> "Rings in ${mins}m"
        }
    }

    override fun formatCustomDaysMask(mask: Int): String {
        val selected = shortWeekdays.indices.filter { (mask and (1 shl it)) != 0 }
        return when {
            selected.isEmpty() -> "No days"
            selected.size == 7 -> "Every day"
            selected == listOf(0, 1, 2, 3, 4) -> "Mon–Fri"
            selected == listOf(5, 6) -> "Sat & Sun"
            else -> selected.joinToString(", ") { shortWeekdays[it] }
        }
    }

    override fun localizeHolidayLabel(rawLabel: String, dayType: ChinaDayType): String {
        val trimmed = rawLabel.trim()
        if (trimmed.isEmpty()) return dayTypeDisplayName(dayType)
        val exactMap = mapOf(
            "工作日" to "Workday",
            "普通工作日" to "Workday",
            "周末" to "Weekend",
            "普通周末" to "Weekend",
            "法定节假日" to "Public Holiday",
            "调休补班" to "Lieu Workday",
            "元旦" to "New Year's Day",
            "春节" to "Spring Festival",
            "清明节" to "Qingming Festival",
            "劳动节" to "Labor Day",
            "端午节" to "Dragon Boat Festival",
            "中秋节" to "Mid-Autumn Festival",
            "国庆节" to "National Day",
            "国庆中秋" to "National Day & Mid-Autumn",
            "稍后提醒" to "Snooze"
        )
        exactMap[trimmed]?.let { return it }

        val isLieuSuffix = trimmed.contains("调休") || trimmed.contains("补班")
        val baseName = trimmed
            .replace("调休补班", "")
            .replace("补班", "")
            .replace("调休", "")
            .trim()
        val translatedBase = exactMap[baseName]
        return when {
            translatedBase != null && isLieuSuffix -> "$translatedBase (Lieu)"
            translatedBase != null -> translatedBase
            isLieuSuffix -> "$baseName (Lieu)".trim()
            else -> trimmed
        }
    }

    override fun skipReasonForDate(
        alarm: AlarmEntity,
        group: AlarmGroupEntity?,
        date: SimpleDate,
        holidayMap: Map<String, HolidayDayEntity>
    ): String? {
        if (!alarm.isEnabled) return "Alarm off"
        if (group != null && !group.isEnabled) return "Group \"${group.name}\" off"
        val dateIso = date.toIsoString()
        if (group?.skippedDateIso == dateIso) return "Group skipped"
        if (alarm.skippedDateIso == dateIso) return "Skipped manually"

        val dayInfo = AlarmScheduleCalculator.resolveDay(date, holidayMap)
        val rule = alarm.effectiveRuleMode(group)
        if (AlarmScheduleCalculator.shouldRingOnDate(alarm, group, date, holidayMap)) return null

        val localizedDay = localizeHolidayLabel(dayInfo.label, dayInfo.dayType)
        return when (rule) {
            HolidayRuleMode.LEGAL_WORKDAY -> when (dayInfo.dayType) {
                ChinaDayType.STATUTORY_HOLIDAY -> "Paused for $localizedDay"
                ChinaDayType.WEEKEND -> "Paused on weekend"
                else -> "Non-workday"
            }
            HolidayRuleMode.LEGAL_HOLIDAY_AND_WEEKEND -> when (dayInfo.dayType) {
                ChinaDayType.LIEU_WORKDAY -> "Paused for $localizedDay"
                ChinaDayType.WORKDAY -> "Paused on workday"
                else -> "Not a holiday"
            }
            HolidayRuleMode.CUSTOM_SKIP_HOLIDAY -> "Not a selected weekday"
            else -> null
        }
    }

    override val msgGroupEnabled = "Group enabled & rescheduled"
    override val msgGroupDisabled = "Alarm group turned off"
    override fun msgGroupSkipRestored(groupName: String) = "Restored next ring for \"$groupName\""
    override fun msgGroupSkipped(groupName: String, dateShort: String) = "Skipped \"$groupName\" on $dateShort"
    override val msgAlarmSkipRestored = "Restored next alarm"
    override fun msgAlarmSkipped(dateShort: String) = "Skipped alarm on $dateShort"
    override val msgAlarmSaved = "Alarm saved & scheduled"
    override fun msgAlarmDeleted(name: String) = "Deleted alarm \"$name\""
    override fun msgGroupSaved(name: String) = "Saved group \"$name\""
    override val msgGroupDeleted = "Group deleted (alarms moved to standalone)"
    override fun msgSyncSuccess(years: List<Int>, count: Int, source: String) =
        "Synced ${years.joinToString("/")} ($count entries)"
    override val msgSyncFailedFallback = "Sync failed; kept built-in and cached holiday schedule"
    override fun msgManualOverrideSet(dateShort: String, typeName: String) =
        "Set $dateShort as $typeName & rescheduled"
    override fun msgManualRangeSet(count: Int, typeName: String) =
        "Set $count days as $typeName & rescheduled"
    override fun msgOverrideReverted(dateShort: String) = "Restored $dateShort to official schedule"
    override val msgAllOverridesReset = "Cleared all custom overrides"
    override fun msgImportSuccess(count: Int) = "Imported $count entries & rescheduled"
    override val msgImportFailed = "Import failed; please check format"
}

object StringPacks {
    fun forLanguage(language: AppLanguage, systemLocale: Locale = Locale.getDefault()): AppStrings {
        return when (AppLanguage.resolveEffective(language, systemLocale)) {
            AppLanguage.ZH_CN -> ZhCnStrings
            AppLanguage.EN -> EnStrings
            AppLanguage.SYSTEM -> ZhCnStrings
        }
    }
}

val LocalAppStrings = staticCompositionLocalOf<AppStrings> { ZhCnStrings }
