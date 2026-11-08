package com.example.lovedays.ui.settings

import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lovedays.core.time.ClockProvider
import com.example.lovedays.data.datastore.AccentColor
import com.example.lovedays.data.datastore.CoupleSettings
import com.example.lovedays.data.datastore.SettingsRepository
import com.example.lovedays.data.datastore.ThemeMode
import com.example.lovedays.data.db.NotificationLogDao
import com.example.lovedays.data.image.ImageStorage
import com.example.lovedays.data.repository.SpecialDayRepository
import com.example.lovedays.notification.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class AppLanguage(val tag: String) { VIETNAMESE("vi"), ENGLISH("en") }

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val specialDayRepository: SpecialDayRepository,
    private val notificationLogDao: NotificationLogDao,
    private val imageStorage: ImageStorage,
    private val scheduler: ReminderScheduler,
    clock: ClockProvider,
) : ViewModel() {

    val today: LocalDate = clock.today()

    val settings: StateFlow<CoupleSettings?> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setNames(my: String, partner: String) {
        if (!CoupleSettings.isValidName(my) || !CoupleSettings.isValidName(partner)) return
        viewModelScope.launch { settingsRepository.setNames(my, partner) }
    }

    fun pickMyAvatar(uri: Uri) = viewModelScope.launch {
        val path = imageStorage.saveAvatar(uri, "me") ?: return@launch
        imageStorage.delete(settings.value?.myAvatarPath)
        settingsRepository.setMyAvatar(path)
    }

    fun pickPartnerAvatar(uri: Uri) = viewModelScope.launch {
        val path = imageStorage.saveAvatar(uri, "partner") ?: return@launch
        imageStorage.delete(settings.value?.partnerAvatarPath)
        settingsRepository.setPartnerAvatar(path)
    }

    fun removeMyAvatar() = viewModelScope.launch {
        imageStorage.delete(settings.value?.myAvatarPath)
        settingsRepository.setMyAvatar(null)
    }

    fun removePartnerAvatar() = viewModelScope.launch {
        imageStorage.delete(settings.value?.partnerAvatarPath)
        settingsRepository.setPartnerAvatar(null)
    }

    fun pickBackground(uri: Uri) = viewModelScope.launch {
        val path = imageStorage.saveBackground(uri) ?: return@launch
        imageStorage.delete(settings.value?.backgroundPath)
        settingsRepository.setBackgroundPath(path)
    }

    fun removeBackground() = viewModelScope.launch {
        imageStorage.delete(settings.value?.backgroundPath)
        settingsRepository.setBackgroundPath(null)
    }

    fun setStartDate(date: LocalDate) {
        if (date.isAfter(today)) return
        viewModelScope.launch { settingsRepository.setStartDate(date) }
    }

    fun setCountStartAsDayOne(v: Boolean) = viewModelScope.launch { settingsRepository.setCountStartAsDayOne(v) }
    fun setNotificationsEnabled(v: Boolean) = viewModelScope.launch { settingsRepository.setNotificationsEnabled(v) }
    fun setRemindOneDayBefore(v: Boolean) = viewModelScope.launch { settingsRepository.setRemindOneDayBefore(v) }
    fun setRemindSevenDaysBefore(v: Boolean) = viewModelScope.launch { settingsRepository.setRemindSevenDaysBefore(v) }
    fun setThemeMode(v: ThemeMode) = viewModelScope.launch { settingsRepository.setThemeMode(v) }
    fun setAccentColor(v: AccentColor) = viewModelScope.launch { settingsRepository.setAccentColor(v) }

    fun setReminderTime(hour: Int, minute: Int) = viewModelScope.launch {
        settingsRepository.setReminderTime(hour, minute)
        scheduler.reschedule(hour, minute)
    }

    fun currentLanguage(): AppLanguage {
        val tag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        val effective = tag.ifEmpty { LocaleListCompat.getAdjustedDefault().toLanguageTags() }
        return if (effective.startsWith("en")) AppLanguage.ENGLISH else AppLanguage.VIETNAMESE
    }

    fun setLanguage(lang: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang.tag))
    }

    /** Xoá toàn bộ dữ liệu; giao diện tự quay về onboarding vì onboardingCompleted = false. */
    fun resetAll() = viewModelScope.launch {
        specialDayRepository.deleteAll()
        notificationLogDao.deleteAll()
        imageStorage.deleteAll()
        settingsRepository.clear()
    }
}
