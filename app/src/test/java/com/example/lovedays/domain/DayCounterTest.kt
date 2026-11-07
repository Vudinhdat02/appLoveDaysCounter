package com.example.lovedays.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId

class DayCounterTest {

    @Test
    fun sameDay_dayOneOn_isOne_dayOneOff_isZero() {
        val d = LocalDate.of(2025, 5, 20)
        assertEquals(1, DayCounter.displayedDays(d, d, countStartAsDayOne = true))
        assertEquals(0, DayCounter.displayedDays(d, d, countStartAsDayOne = false))
    }

    @Test
    fun nextDay_increasesByOne() {
        val start = LocalDate.of(2025, 1, 1)
        val today = LocalDate.of(2025, 3, 1)
        val before = DayCounter.displayedDays(start, today, true)
        val after = DayCounter.displayedDays(start, today.plusDays(1), true)
        assertEquals(before + 1, after)
    }

    @Test
    fun acrossLeapYear_countsFebruary29() {
        val start = LocalDate.of(2024, 2, 1)
        val today = LocalDate.of(2024, 3, 1)
        assertEquals(29, DayCounter.daysBetween(start, today))
        assertEquals(366, DayCounter.daysBetween(LocalDate.of(2024, 1, 1), LocalDate.of(2025, 1, 1)))
    }

    @Test
    fun startOnFeb29_periodOnFeb28NextYear_doesNotCrash() {
        val start = LocalDate.of(2024, 2, 29)
        val today = LocalDate.of(2025, 2, 28)
        val period = DayCounter.period(start, today)
        assertEquals(Period.of(0, 11, 30), period)
        assertEquals(365, DayCounter.daysBetween(start, today))
        assertEquals(366, DayCounter.displayedDays(start, today, true))
        assertEquals(Period.of(1, 0, 1), DayCounter.period(start, LocalDate.of(2025, 3, 1)))
    }

    @Test
    fun period_yearsMonthsDays() {
        val p = DayCounter.period(LocalDate.of(2020, 1, 15), LocalDate.of(2023, 4, 20))
        assertEquals(3, p.years)
        assertEquals(3, p.months)
        assertEquals(5, p.days)
    }

    @Test
    fun futureStart_neverNegative() {
        val start = LocalDate.of(2030, 1, 1)
        val today = LocalDate.of(2025, 1, 1)
        assertEquals(0, DayCounter.displayedDays(start, today, true))
        assertEquals(Period.ZERO, DayCounter.period(start, today))
    }

    @Test
    fun valueInUnits() {
        val start = LocalDate.of(2025, 1, 1)
        val today = LocalDate.of(2025, 4, 10) // ngày thứ 100
        assertEquals(100, DayCounter.valueIn(CounterUnit.DAYS, start, today, true))
        assertEquals(14, DayCounter.valueIn(CounterUnit.WEEKS, start, today, true))
        assertEquals(3, DayCounter.valueIn(CounterUnit.MONTHS, start, today, true))
        assertEquals(2400, DayCounter.valueIn(CounterUnit.HOURS, start, today, true))
    }

    @Test
    fun timezoneChange_changesToday() {
        // 23:30 UTC ngày 1/1 là 06:30 ngày 2/1 ở Việt Nam.
        val instant = Instant.parse("2025-01-01T23:30:00Z")
        val start = LocalDate.of(2025, 1, 1)
        val utcToday = DayCounter.todayIn(instant, ZoneId.of("UTC"))
        val vnToday = DayCounter.todayIn(instant, ZoneId.of("Asia/Ho_Chi_Minh"))
        assertEquals(1, DayCounter.displayedDays(start, utcToday, true))
        assertEquals(2, DayCounter.displayedDays(start, vnToday, true))
    }

    @Test
    fun counterUnit_cycles() {
        assertEquals(CounterUnit.WEEKS, CounterUnit.DAYS.next())
        assertEquals(CounterUnit.DAYS, CounterUnit.HOURS.next())
    }
}
