package com.example.lovedays.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val ONBOARDING = booleanPreferencesKey("onboarding_completed")
        val MY_NAME = stringPreferencesKey("my_name")
        val PARTNER_NAME = stringPreferencesKey("partner_name")
        val MY_AVATAR = stringPreferencesKey("my_avatar_path")
        val PARTNER_AVATAR = stringPreferencesKey("partner_avatar_path")
        val START_DATE = longPreferencesKey("start_date_epoch_day")
        val DAY_ONE = booleanPreferencesKey("count_start_as_day_one")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val ONE_DAY = booleanPreferencesKey("remind_one_day_before")
        val SEVEN_DAYS = booleanPreferencesKey("remind_seven_days_before")
        val THEME = stringPreferencesKey("theme_mode")
        val ACCENT = stringPreferencesKey("accent_color")
    }

    val settings: Flow<CoupleSettings> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it.toSettings() }
        .distinctUntilChanged()

    suspend fun current(): CoupleSettings = settings.first()

    suspend fun completeOnboarding(
        myName: String,
        partnerName: String,
        startDate: LocalDate,
        myAvatarPath: String?,
        partnerAvatarPath: String?,
    ) = dataStore.edit {
        it[Keys.MY_NAME] = myName.trim()
        it[Keys.PARTNER_NAME] = partnerName.trim()
        it[Keys.START_DATE] = startDate.toEpochDay()
        it.setOrRemove(Keys.MY_AVATAR, myAvatarPath)
        it.setOrRemove(Keys.PARTNER_AVATAR, partnerAvatarPath)
        it[Keys.ONBOARDING] = true
    }

    suspend fun setNames(myName: String, partnerName: String) = dataStore.edit {
        it[Keys.MY_NAME] = myName.trim()
        it[Keys.PARTNER_NAME] = partnerName.trim()
    }

    suspend fun setMyAvatar(path: String?) = dataStore.edit { it.setOrRemove(Keys.MY_AVATAR, path) }
    suspend fun setPartnerAvatar(path: String?) = dataStore.edit { it.setOrRemove(Keys.PARTNER_AVATAR, path) }
    suspend fun setStartDate(date: LocalDate) = dataStore.edit { it[Keys.START_DATE] = date.toEpochDay() }
    suspend fun setCountStartAsDayOne(value: Boolean) = dataStore.edit { it[Keys.DAY_ONE] = value }
    suspend fun setNotificationsEnabled(value: Boolean) = dataStore.edit { it[Keys.NOTIFICATIONS] = value }
    suspend fun setReminderTime(hour: Int, minute: Int) = dataStore.edit {
        it[Keys.REMINDER_HOUR] = hour.coerceIn(0, 23)
        it[Keys.REMINDER_MINUTE] = minute.coerceIn(0, 59)
    }
    suspend fun setRemindOneDayBefore(value: Boolean) = dataStore.edit { it[Keys.ONE_DAY] = value }
    suspend fun setRemindSevenDaysBefore(value: Boolean) = dataStore.edit { it[Keys.SEVEN_DAYS] = value }
    suspend fun setThemeMode(mode: ThemeMode) = dataStore.edit { it[Keys.THEME] = mode.name }
    suspend fun setAccentColor(color: AccentColor) = dataStore.edit { it[Keys.ACCENT] = color.name }

    suspend fun clear() = dataStore.edit { it.clear() }

    private fun <T> MutablePreferences.setOrRemove(key: Preferences.Key<T>, value: T?) {
        if (value == null) remove(key) else this[key] = value
    }

    private fun Preferences.toSettings(): CoupleSettings {
        val d = CoupleSettings()
        return CoupleSettings(
            onboardingCompleted = this[Keys.ONBOARDING] ?: d.onboardingCompleted,
            myName = this[Keys.MY_NAME] ?: d.myName,
            partnerName = this[Keys.PARTNER_NAME] ?: d.partnerName,
            myAvatarPath = this[Keys.MY_AVATAR],
            partnerAvatarPath = this[Keys.PARTNER_AVATAR],
            startDate = this[Keys.START_DATE]?.let(LocalDate::ofEpochDay),
            countStartAsDayOne = this[Keys.DAY_ONE] ?: d.countStartAsDayOne,
            notificationsEnabled = this[Keys.NOTIFICATIONS] ?: d.notificationsEnabled,
            reminderHour = this[Keys.REMINDER_HOUR] ?: d.reminderHour,
            reminderMinute = this[Keys.REMINDER_MINUTE] ?: d.reminderMinute,
            remindOneDayBefore = this[Keys.ONE_DAY] ?: d.remindOneDayBefore,
            remindSevenDaysBefore = this[Keys.SEVEN_DAYS] ?: d.remindSevenDaysBefore,
            themeMode = this[Keys.THEME]?.let { v -> ThemeMode.entries.firstOrNull { it.name == v } } ?: d.themeMode,
            accentColor = this[Keys.ACCENT]?.let { v -> AccentColor.entries.firstOrNull { it.name == v } } ?: d.accentColor,
        )
    }
}
