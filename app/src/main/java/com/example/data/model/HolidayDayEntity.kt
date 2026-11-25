package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holiday_days")
data class HolidayDayEntity(
    @PrimaryKey
    val dateIso: String,
    val year: Int,
    val name: String,
    val isOffDay: Boolean,
    /**
     * Optional explicit override type when isUserOverride is true:
     * "STATUTORY_HOLIDAY", "LIEU_WORKDAY", "WORKDAY", "WEEKEND"
     */
    val overrideType: String? = null,
    val isUserOverride: Boolean = false,
    val source: String = "builtin",
    val updatedAtMillis: Long = System.currentTimeMillis()
)
