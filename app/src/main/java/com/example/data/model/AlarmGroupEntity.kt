package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_groups")
data class AlarmGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val description: String = "",
    val iconKey: String = "work",
    val accentColorHex: Long = 0xFF387AFF,
    val isEnabled: Boolean = true,
    val defaultRuleMode: String = HolidayRuleMode.LEGAL_WORKDAY.name,
    val customDaysMask: Int = 0b0011111, // Mon..Fri
    val includeLieuWorkdays: Boolean = true,
    val skippedDateIso: String? = null,
    val sortOrder: Int = 0
) {
    val ruleModeEnum: HolidayRuleMode
        get() = HolidayRuleMode.fromName(defaultRuleMode)
}
