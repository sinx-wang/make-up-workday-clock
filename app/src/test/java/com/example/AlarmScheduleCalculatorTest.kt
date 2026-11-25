package com.example

import com.example.data.local.DefaultChinaHolidays
import com.example.data.model.AlarmEntity
import com.example.data.model.AlarmGroupEntity
import com.example.data.model.ChinaDayType
import com.example.data.model.HolidayDayEntity
import com.example.data.model.HolidayRuleMode
import com.example.data.model.SimpleDate
import com.example.domain.AlarmScheduleCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmScheduleCalculatorTest {

    private val baselineMap: Map<String, HolidayDayEntity> =
        DefaultChinaHolidays.holidays2026().associateBy { it.dateIso }

    @Test
    fun legalWorkdayGroup_skipsStatutoryHoliday_andRingsOnLieuWeekend() {
        val workGroup = AlarmGroupEntity(
            id = 1L,
            name = "早起通勤组",
            isEnabled = true,
            defaultRuleMode = HolidayRuleMode.LEGAL_WORKDAY.name
        )
        val alarm = AlarmEntity(
            id = 10L,
            groupId = 1L,
            hour = 7,
            minute = 30,
            label = "通勤",
            isEnabled = true,
            inheritGroupRule = true
        )

        // 2026-10-01 is Thursday (National Day statutory holiday) -> MUST SKIP
        val oct1 = SimpleDate(2026, 10, 1)
        assertEquals(ChinaDayType.STATUTORY_HOLIDAY, AlarmScheduleCalculator.resolveDay(oct1, baselineMap).dayType)
        assertFalse(AlarmScheduleCalculator.shouldRingOnDate(alarm, workGroup, oct1, baselineMap))

        // 2026-10-10 is Saturday (National Day lieu workday 调休补班) -> MUST RING
        val oct10 = SimpleDate(2026, 10, 10)
        assertEquals(6, oct10.dayOfWeekIso)
        assertEquals(ChinaDayType.LIEU_WORKDAY, AlarmScheduleCalculator.resolveDay(oct10, baselineMap).dayType)
        assertTrue(AlarmScheduleCalculator.shouldRingOnDate(alarm, workGroup, oct10, baselineMap))

        // 2026-10-11 is regular Sunday -> MUST SKIP
        val oct11 = SimpleDate(2026, 10, 11)
        assertEquals(7, oct11.dayOfWeekIso)
        assertFalse(AlarmScheduleCalculator.shouldRingOnDate(alarm, workGroup, oct11, baselineMap))
    }

    @Test
    fun holidayOnlyGroup_ringsOnStatutoryHoliday_andSkipsLieuWeekend() {
        val holidayGroup = AlarmGroupEntity(
            id = 2L,
            name = "节假慵懒组",
            isEnabled = true,
            defaultRuleMode = HolidayRuleMode.LEGAL_HOLIDAY_AND_WEEKEND.name
        )
        val alarm = AlarmEntity(
            id = 20L,
            groupId = 2L,
            hour = 9,
            minute = 30,
            label = "早午餐",
            isEnabled = true,
            inheritGroupRule = true
        )

        // National Day Thursday -> MUST RING
        assertTrue(AlarmScheduleCalculator.shouldRingOnDate(alarm, holidayGroup, SimpleDate(2026, 10, 1), baselineMap))
        // Lieu workday Saturday 2026-10-10 -> MUST SKIP
        assertFalse(AlarmScheduleCalculator.shouldRingOnDate(alarm, holidayGroup, SimpleDate(2026, 10, 10), baselineMap))
        // Regular Sunday 2026-10-11 -> MUST RING
        assertTrue(AlarmScheduleCalculator.shouldRingOnDate(alarm, holidayGroup, SimpleDate(2026, 10, 11), baselineMap))
    }

    @Test
    fun manualOverride_andGroupMasterSwitch_takePrecedence() {
        val workGroup = AlarmGroupEntity(
            id = 1L,
            name = "早起通勤组",
            isEnabled = true,
            defaultRuleMode = HolidayRuleMode.LEGAL_WORKDAY.name
        )
        val alarm = AlarmEntity(
            id = 30L,
            groupId = 1L,
            hour = 7,
            minute = 0,
            label = "早起",
            isEnabled = true,
            inheritGroupRule = true
        )

        // 2026-11-24 is a regular Tuesday, manually overridden as user holiday
        val customDate = SimpleDate(2026, 11, 24)
        val overriddenMap = baselineMap + (
            customDate.toIsoString() to HolidayDayEntity(
                dateIso = customDate.toIsoString(),
                year = 2026,
                name = "公司年假",
                isOffDay = true,
                overrideType = ChinaDayType.STATUTORY_HOLIDAY.name,
                isUserOverride = true,
                source = "manual"
            )
            )
        assertFalse(AlarmScheduleCalculator.shouldRingOnDate(alarm, workGroup, customDate, overriddenMap))

        // Disabled group disables child alarm on regular workday
        val disabledGroup = workGroup.copy(isEnabled = false)
        assertFalse(AlarmScheduleCalculator.shouldRingOnDate(alarm, disabledGroup, SimpleDate(2026, 11, 25), baselineMap))
    }

    @Test
    fun groupSkipNext_skipsAllGroupAlarmsForSkippedDateOnly() {
        val workGroup = AlarmGroupEntity(
            id = 1L,
            name = "早起通勤组",
            isEnabled = true,
            defaultRuleMode = HolidayRuleMode.LEGAL_WORKDAY.name,
            skippedDateIso = "2026-11-24"
        )
        val alarm = AlarmEntity(
            id = 40L,
            groupId = 1L,
            hour = 7,
            minute = 20,
            label = "起床",
            isEnabled = true,
            inheritGroupRule = true
        )

        // 2026-11-24 skipped at group level -> MUST SKIP
        assertFalse(AlarmScheduleCalculator.shouldRingOnDate(alarm, workGroup, SimpleDate(2026, 11, 24), baselineMap))
        // Next workday 2026-11-25 -> MUST RING
        assertTrue(AlarmScheduleCalculator.shouldRingOnDate(alarm, workGroup, SimpleDate(2026, 11, 25), baselineMap))
    }

    @Test
    fun customWeekdays_followsSelectedDaysStrictlyWithoutHolidayInterference() {
        // Select Thursday (bit 3) and Sunday (bit 6) only
        val customMask = (1 shl 3) or (1 shl 6)
        val customGroup = AlarmGroupEntity(
            id = 3L,
            name = "自定义组",
            isEnabled = true,
            defaultRuleMode = HolidayRuleMode.CUSTOM_SKIP_HOLIDAY.name,
            customDaysMask = customMask
        )
        val alarm = AlarmEntity(
            id = 50L,
            groupId = 3L,
            hour = 8,
            minute = 0,
            label = "自定义提醒",
            isEnabled = true,
            inheritGroupRule = true
        )

        // 2026-10-01 is Thursday (National Day statutory holiday) -> MUST STILL RING because Thursday is selected
        assertTrue(AlarmScheduleCalculator.shouldRingOnDate(alarm, customGroup, SimpleDate(2026, 10, 1), baselineMap))
        // 2026-10-10 is Saturday (National Day lieu workday) -> MUST NOT RING because Saturday is NOT selected
        assertFalse(AlarmScheduleCalculator.shouldRingOnDate(alarm, customGroup, SimpleDate(2026, 10, 10), baselineMap))
        // 2026-10-11 is Sunday -> MUST RING
        assertTrue(AlarmScheduleCalculator.shouldRingOnDate(alarm, customGroup, SimpleDate(2026, 10, 11), baselineMap))
    }
}
