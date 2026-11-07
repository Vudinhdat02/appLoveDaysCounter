package com.example.lovedays.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReminderPlannerTest {

    private val start = LocalDate.of(2025, 1, 1)
    private val allOn = ReminderPreferences(true, remindOneDayBefore = true, remindSevenDaysBefore = true)

    private fun events(today: LocalDate, special: List<SpecialDay> = emptyList()) =
        MilestoneGenerator.allMilestones(start, special, today, countStartAsDayOne = true)

    @Test
    fun todayIsMilestone_sendsSameDay() {
        val today = LocalDate.of(2025, 4, 10) // 100 ngày
        val plan = ReminderPlanner.plan(events(today), today, allOn, emptySet())
        val sameDay = plan.filter { it.trigger == TriggerType.SAME_DAY }
        assertEquals(listOf("milestone_days_100"), sameDay.map { it.milestone.key })
    }

    @Test
    fun oneDayBefore() {
        val today = LocalDate.of(2025, 4, 9)
        val plan = ReminderPlanner.plan(events(today), today, allOn, emptySet())
        assertEquals(listOf(TriggerType.ONE_DAY_BEFORE to "milestone_days_100"), plan.map { it.trigger to it.milestone.key })
    }

    @Test
    fun sevenDaysBefore_onlyWhenEnabled() {
        val today = LocalDate.of(2025, 4, 3)
        val plan = ReminderPlanner.plan(events(today), today, allOn, emptySet())
        assertEquals(listOf(TriggerType.SEVEN_DAYS_BEFORE to "milestone_days_100"), plan.map { it.trigger to it.milestone.key })

        val off = allOn.copy(remindSevenDaysBefore = false)
        assertTrue(ReminderPlanner.plan(events(today), today, off, emptySet()).isEmpty())
    }

    @Test
    fun oneDayBefore_disabled_sendsNothing() {
        val today = LocalDate.of(2025, 4, 9)
        val prefs = allOn.copy(remindOneDayBefore = false)
        assertTrue(ReminderPlanner.plan(events(today), today, prefs, emptySet()).isEmpty())
    }

    @Test
    fun alreadySent_isNotSentAgain() {
        val today = LocalDate.of(2025, 4, 10)
        val first = ReminderPlanner.plan(events(today), today, allOn, emptySet())
        val sent = first.map { it.sentKey }.toSet()
        assertTrue(ReminderPlanner.plan(events(today), today, allOn, sent).isEmpty())
    }

    @Test
    fun notificationsDisabled_sendsNothing() {
        val today = LocalDate.of(2025, 4, 10)
        val prefs = allOn.copy(notificationsEnabled = false)
        assertTrue(ReminderPlanner.plan(events(today), today, prefs, emptySet()).isEmpty())
    }

    @Test
    fun specialDay_respectsRemindFlag() {
        val today = LocalDate.of(2025, 6, 1)
        val on = SpecialDay(id = 1, title = "Sinh nhật", date = LocalDate.of(2000, 6, 1), repeatYearly = true)
        val off = SpecialDay(id = 2, title = "Gặp nhau", date = LocalDate.of(2024, 6, 1), repeatYearly = true, remindEnabled = false)
        val plan = ReminderPlanner.plan(events(today, listOf(on, off)), today, allOn, emptySet())
        assertEquals(listOf("special_1"), plan.map { it.milestone.key })
        assertEquals(TriggerType.SAME_DAY, plan.single().trigger)
    }
}
