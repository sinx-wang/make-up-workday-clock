package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alarms",
    foreignKeys = [
        ForeignKey(
            entity = AlarmGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["groupId"])]
)
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val groupId: Long? = null,
    val hour: Int,
    val minute: Int,
    val label: String,
    val isEnabled: Boolean = true,
    val inheritGroupRule: Boolean = true,
    val ruleMode: String = HolidayRuleMode.LEGAL_WORKDAY.name,
    val customDaysMask: Int = 0b0011111,
    val includeLieuWorkdays: Boolean = true,
    val vibrate: Boolean = true,
    val gradualVolume: Boolean = true,
    val snoozeMinutes: Int = 5,
    val ringtoneUri: String? = null,
    val ringtoneTitle: String = "系统默认铃声",
    val skippedDateIso: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun formattedTime(): String =
        String.format(java.util.Locale.US, "%02d:%02d", hour, minute)

    fun effectiveRuleMode(group: AlarmGroupEntity?): HolidayRuleMode =
        if (group != null && inheritGroupRule) group.ruleModeEnum
        else HolidayRuleMode.fromName(ruleMode)

    fun effectiveCustomDaysMask(group: AlarmGroupEntity?): Int =
        if (group != null && inheritGroupRule) group.customDaysMask
        else customDaysMask

    fun effectiveIncludeLieu(group: AlarmGroupEntity?): Boolean =
        if (group != null && inheritGroupRule) group.includeLieuWorkdays
        else includeLieuWorkdays

    fun isEffectivelyEnabled(group: AlarmGroupEntity?): Boolean =
        isEnabled && (group == null || group.isEnabled)
}
