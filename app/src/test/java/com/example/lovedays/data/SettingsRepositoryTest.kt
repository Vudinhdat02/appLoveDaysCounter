package com.example.lovedays.data

import com.example.lovedays.data.datastore.AccentColor
import com.example.lovedays.data.datastore.ThemeMode
import com.example.lovedays.testutil.testSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.LocalDate

class SettingsRepositoryTest {

    @get:Rule val tmp = TemporaryFolder()

    @Test
    fun defaults_thenOnboarding_thenClear() = runTest {
        val repo = testSettingsRepository(tmp.root)
        val initial = repo.current()
        assertFalse(initial.onboardingCompleted)
        assertTrue(initial.countStartAsDayOne)
        assertEquals(8, initial.reminderHour)
        assertTrue(initial.remindOneDayBefore)
        assertFalse(initial.remindSevenDaysBefore)
        assertEquals(ThemeMode.SYSTEM, initial.themeMode)
        assertEquals(AccentColor.PINK, initial.accentColor)

        repo.completeOnboarding(" Đạt ", "Linh", LocalDate.of(2024, 2, 29), null, "/x.jpg")
        repo.setAccentColor(AccentColor.PURPLE)
        val s = repo.current()
        assertTrue(s.onboardingCompleted)
        assertEquals("Đạt", s.myName)
        assertEquals(LocalDate.of(2024, 2, 29), s.startDate)
        assertEquals("/x.jpg", s.partnerAvatarPath)
        assertEquals(AccentColor.PURPLE, s.accentColor)

        repo.clear()
        assertFalse(repo.current().onboardingCompleted)
    }
}
