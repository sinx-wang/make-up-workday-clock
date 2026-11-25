package com.example.data.model

enum class ChinaDayType(val shortBadge: String, val displayName: String) {
    WORKDAY("班", "普通工作日"),
    WEEKEND("休", "普通周末"),
    STATUTORY_HOLIDAY("休", "法定节假日"),
    LIEU_WORKDAY("班", "调休补班")
}

enum class HolidayRuleMode(
    val title: String,
    val shortTag: String,
    val description: String
) {
    LEGAL_WORKDAY(
        title = "智能工作日（含调休补班）",
        shortTag = "法定工作日",
        description = "法定节假日自动关闭，调休补班周末自动开启"
    ),
    LEGAL_HOLIDAY_AND_WEEKEND(
        title = "仅节假日与周末（含调休）",
        shortTag = "仅休假日",
        description = "周末及法定节假日自动开启，调休补班自动关闭"
    ),
    CUSTOM_SKIP_HOLIDAY(
        title = "自定义星期",
        shortTag = "自定义星期",
        description = "按选定星期规律响铃"
    ),
    EVERY_DAY(
        title = "每天响铃",
        shortTag = "每天",
        description = "不受节假日与调休影响，每日按时响铃"
    ),
    ONCE(
        title = "仅响一次",
        shortTag = "单次",
        description = "在下一个到达该时间点响铃一次后自动关闭"
    );

    companion object {
        fun fromName(name: String): HolidayRuleMode =
            entries.firstOrNull { it.name == name } ?: LEGAL_WORKDAY
    }
}

data class ResolvedDayInfo(
    val date: SimpleDate,
    val dayType: ChinaDayType,
    val label: String,
    val isUserOverride: Boolean = false,
    val source: String = "default"
) {
    val isWorkingDay: Boolean
        get() = dayType == ChinaDayType.WORKDAY || dayType == ChinaDayType.LIEU_WORKDAY

    val isOffDay: Boolean
        get() = dayType == ChinaDayType.WEEKEND || dayType == ChinaDayType.STATUTORY_HOLIDAY
}
