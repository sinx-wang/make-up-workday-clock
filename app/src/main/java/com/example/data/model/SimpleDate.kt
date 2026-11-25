package com.example.data.model

import java.util.Calendar
import java.util.Locale

/**
 * Lightweight, minSdk 24+ safe Gregorian calendar date value object.
 * Uses Julian Day Number math for exact leap-year, day-of-week, and date arithmetic.
 */
data class SimpleDate(
    val year: Int,
    val month: Int, // 1..12
    val day: Int    // 1..31
) : Comparable<SimpleDate> {

    /**
     * ISO-8601 day of week: 1 = Monday, ..., 6 = Saturday, 7 = Sunday.
     */
    val dayOfWeekIso: Int
        get() = ((toEpochDay() + 3).mod(7)) + 1

    val isRegularWeekend: Boolean
        get() = dayOfWeekIso == 6 || dayOfWeekIso == 7

    fun toIsoString(): String =
        String.format(Locale.US, "%04d-%02d-%02d", year, month, day)

    fun toChineseShort(): String = "${month}月${day}日"

    fun toChineseWeekday(): String = when (dayOfWeekIso) {
        1 -> "周一"
        2 -> "周二"
        3 -> "周三"
        4 -> "周四"
        5 -> "周五"
        6 -> "周六"
        7 -> "周日"
        else -> ""
    }

    fun plusDays(days: Int): SimpleDate = fromEpochDay(toEpochDay() + days)

    fun daysUntil(other: SimpleDate): Int = (other.toEpochDay() - this.toEpochDay()).toInt()

    fun toEpochDay(): Long {
        val y = year.toLong()
        val m = month.toLong()
        val d = day.toLong()
        val a = (14 - m) / 12
        val yAdj = y + 4800 - a
        val mAdj = m + 12 * a - 3
        val jdn = d + (153 * mAdj + 2) / 5 + 365 * yAdj + yAdj / 4 - yAdj / 100 + yAdj / 400 - 32045
        return jdn - 2440588L // 1970-01-01 is JDN 2440588
    }

    fun toTriggerMillis(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    override fun compareTo(other: SimpleDate): Int =
        this.toEpochDay().compareTo(other.toEpochDay())

    companion object {
        fun today(): SimpleDate {
            val cal = Calendar.getInstance()
            return SimpleDate(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                day = cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun fromMillis(millis: Long): SimpleDate {
            val cal = Calendar.getInstance().apply { timeInMillis = millis }
            return SimpleDate(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                day = cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun parseIso(iso: String): SimpleDate? {
            val parts = iso.trim().split("-")
            if (parts.size != 3) return null
            val y = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            val d = parts[2].toIntOrNull() ?: return null
            if (m !in 1..12 || d !in 1..daysInMonth(y, m)) return null
            return SimpleDate(y, m, d)
        }

        fun fromEpochDay(epochDay: Long): SimpleDate {
            val jdn = epochDay + 2440588L
            val a = jdn + 32044
            val b = (4 * a + 3) / 146097
            val c = a - (146097 * b) / 4
            val d = (4 * c + 3) / 1461
            val e = c - (1461 * d) / 4
            val m = (5 * e + 2) / 153
            val day = (e - (153 * m + 2) / 5 + 1).toInt()
            val month = (m + 3 - 12 * (m / 10)).toInt()
            val year = (100 * b + d - 4800 + (m / 10)).toInt()
            return SimpleDate(year, month, day)
        }

        fun isLeapYear(year: Int): Boolean =
            (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

        fun daysInMonth(year: Int, month: Int): Int = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 30
        }
    }
}
