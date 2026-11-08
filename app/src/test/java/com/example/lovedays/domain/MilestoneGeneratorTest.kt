package com.example.lovedays.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MilestoneGeneratorTest {

    private val start = LocalDate.of(2025, 1, 1)

    private fun List<Milestone>.byKey(key: String) = first { it.key == key }

    @Test
    fun day100_dayOneOn_isApril10() {
        val list = MilestoneGenerator.autoMilestones(start, LocalDate.of(2025, 1, 2), countStartAsDayOne = true)
        assertEquals(LocalDate.of(2025, 4, 10), list.byKey("milestone_days_100").date)
    }

    @Test
    fun day100_dayOneOff_isApril11() {
        val list = MilestoneGenerator.autoMilestones(start, LocalDate.of(2025, 1, 2), countStartAsDayOne = false)
        assertEquals(LocalDate.of(2025, 4, 11), list.byKey("milestone_days_100").date)
    }

    @Test
    fun days500_1000_dates_andNo365Multiples() {
        val list = MilestoneGenerator.autoMilestones(start, LocalDate.of(2025, 1, 2), true, horizonDays = 1500)
        assertTrue(list.none { it.key == "milestone_days_365" || it.key == "milestone_days_1460" })
        assertEquals(start.plusDays(499), list.byKey("milestone_days_500").date)
        assertEquals(start.plusDays(999), list.byKey("milestone_days_1000").date)
        assertEquals(LocalDate.of(2026, 1, 1), list.byKey("milestone_year_1").date)
    }

    @Test
    fun dayValues_followRules() {
        assertEquals(
            listOf(100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1500, 2000),
            MilestoneGenerator.dayMilestoneValues(2000),
        )
    }

    @Test
    fun yearMilestone_fromFeb29_usesFeb28InNonLeapYears() {
        val s = LocalDate.of(2024, 2, 29)
        val list = MilestoneGenerator.autoMilestones(s, LocalDate.of(2024, 3, 1), true, horizonDays = 1500)
        assertEquals(LocalDate.of(2025, 2, 28), list.byKey("milestone_year_1").date)
        assertEquals(LocalDate.of(2027, 2, 28), list.byKey("milestone_year_3").date)
        assertEquals(LocalDate.of(2028, 2, 29), MilestoneGenerator.dateOfYearMilestone(s, 4))
    }

    @Test
    fun monthMilestones_skipMultiplesOf12_andClampEndOfMonth() {
        val s = LocalDate.of(2025, 1, 31)
        val list = MilestoneGenerator.autoMilestones(s, LocalDate.of(2025, 2, 1), true, horizonDays = 800)
        assertEquals(LocalDate.of(2025, 2, 28), list.byKey("milestone_month_1").date)
        assertEquals(LocalDate.of(2025, 3, 31), list.byKey("milestone_month_2").date)
        assertTrue(list.none { it.key == "milestone_month_12" || it.key == "milestone_month_24" })
        assertEquals(LocalDate.of(2026, 1, 31), list.byKey("milestone_year_1").date)
        assertEquals(LocalDate.of(2026, 2, 28), list.byKey("milestone_month_13").date)
    }

    @Test
    fun sortedAscending_andPassedFlags() {
        val today = LocalDate.of(2025, 6, 1)
        val list = MilestoneGenerator.allMilestones(start, emptyList(), today, true)
        assertEquals(list.sortedBy { it.date }, list)
        assertTrue(list.byKey("milestone_days_100").isPassed)
        assertTrue(list.byKey("milestone_month_4").isPassed)
        assertFalse(list.byKey("milestone_month_5").isPassed) // đúng hôm nay
        assertFalse(list.byKey("milestone_days_200").isPassed)
        assertEquals("milestone_month_5", MilestoneGenerator.nextUpcoming(list)!!.key)
        val next = MilestoneGenerator.nextUpcoming(list.filter { it.type == MilestoneType.DAYS })!!
        assertEquals("milestone_days_200", next.key)
        assertEquals(LocalDate.of(2025, 7, 19), next.date)
        assertEquals(48, next.daysRemaining)
    }

    @Test
    fun todayMilestone_isNotPassed() {
        val today = LocalDate.of(2025, 4, 10)
        val list = MilestoneGenerator.allMilestones(start, emptyList(), today, true)
        val next = MilestoneGenerator.nextUpcoming(list)!!
        assertEquals("milestone_days_100", next.key)
        assertEquals(0, next.daysRemaining)
    }

    @Test
    fun repeatingSpecialDay_showsNextOccurrence() {
        val today = LocalDate.of(2025, 6, 1)
        val birthday = SpecialDay(id = 7, title = "Sinh nhật em", date = LocalDate.of(1999, 3, 8), repeatYearly = true)
        val m = MilestoneGenerator.specialDayMilestones(listOf(birthday), today).single()
        assertEquals(LocalDate.of(2026, 3, 8), m.date)
        assertFalse(m.isPassed)
        assertEquals(280, m.daysRemaining)
        assertEquals("special_7", m.key)
    }

    @Test
    fun repeatingSpecialDay_laterThisYear_andToday() {
        val today = LocalDate.of(2025, 6, 1)
        assertEquals(
            LocalDate.of(2025, 12, 24),
            MilestoneGenerator.nextOccurrence(LocalDate.of(2020, 12, 24), true, today),
        )
        assertEquals(today, MilestoneGenerator.nextOccurrence(LocalDate.of(2010, 6, 1), true, today))
        assertEquals(
            LocalDate.of(2025, 2, 28),
            MilestoneGenerator.nextOccurrence(LocalDate.of(2024, 2, 29), true, LocalDate.of(2025, 1, 10)),
        )
    }

    @Test
    fun nonRepeatingSpecialDay_inPast_isPassed() {
        val today = LocalDate.of(2025, 6, 1)
        val d = SpecialDay(id = 1, title = "Cầu hôn", date = LocalDate.of(2025, 2, 14), repeatYearly = false)
        val m = MilestoneGenerator.specialDayMilestones(listOf(d), today).single()
        assertTrue(m.isPassed)
        assertTrue(m.daysRemaining < 0)
    }
}
