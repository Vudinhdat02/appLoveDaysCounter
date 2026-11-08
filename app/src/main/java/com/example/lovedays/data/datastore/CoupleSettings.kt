package com.example.lovedays.data.datastore

import com.example.lovedays.domain.ReminderPreferences
import java.time.LocalDate

enum class ThemeMode { LIGHT, DARK, SYSTEM }

enum class AccentColor { PINK, RED, PURPLE, BLUE, WHITE, BLACK, DYNAMIC }

/** Hình dạng ô hiển thị số ngày ở màn hình chính. */
enum class CounterShape { ROUNDED, CIRCLE, HEART, FLOWER }

data class CoupleSettings(
    val onboardingCompleted: Boolean = false,
    val myName: String = "",
    val partnerName: String = "",
    val myAvatarPath: String? = null,
    val partnerAvatarPath: String? = null,
    val startDate: LocalDate? = null,
    val countStartAsDayOne: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val remindOneDayBefore: Boolean = true,
    val remindSevenDaysBefore: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: AccentColor = AccentColor.PINK,
    val counterShape: CounterShape = CounterShape.ROUNDED,
    val backgroundPath: String? = null,
) {
    val reminderPreferences: ReminderPreferences
        get() = ReminderPreferences(notificationsEnabled, remindOneDayBefore, remindSevenDaysBefore)

    companion object {
        const val NAME_MAX_LENGTH = 30
        fun isValidName(name: String) = name.trim().length in 1..NAME_MAX_LENGTH
    }
}
