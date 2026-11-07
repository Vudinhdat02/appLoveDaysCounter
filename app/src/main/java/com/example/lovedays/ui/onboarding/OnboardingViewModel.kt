package com.example.lovedays.ui.onboarding

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lovedays.core.time.ClockProvider
import com.example.lovedays.data.datastore.CoupleSettings
import com.example.lovedays.data.datastore.SettingsRepository
import com.example.lovedays.data.image.ImageStorage
import com.example.lovedays.notification.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class OnboardingStep { WELCOME, NAMES, START_DATE, AVATARS, NOTIFICATIONS }

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val myName: String = "",
    val partnerName: String = "",
    val startDate: LocalDate? = null,
    val myAvatarPath: String? = null,
    val partnerAvatarPath: String? = null,
) {
    val namesValid: Boolean
        get() = CoupleSettings.isValidName(myName) && CoupleSettings.isValidName(partnerName)
}

/** Trạng thái được giữ trong SavedStateHandle để không mất khi xoay màn hình hoặc bị kill process. */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    private val imageStorage: ImageStorage,
    private val scheduler: ReminderScheduler,
    clock: ClockProvider,
) : ViewModel() {

    val today: LocalDate = clock.today()

    private val step = savedStateHandle.getStateFlow(KEY_STEP, OnboardingStep.WELCOME.name)
    private val myName = savedStateHandle.getStateFlow(KEY_MY_NAME, "")
    private val partnerName = savedStateHandle.getStateFlow(KEY_PARTNER_NAME, "")
    private val startDate = savedStateHandle.getStateFlow<Long?>(KEY_START, null)
    private val myAvatar = savedStateHandle.getStateFlow<String?>(KEY_MY_AVATAR, null)
    private val partnerAvatar = savedStateHandle.getStateFlow<String?>(KEY_PARTNER_AVATAR, null)

    val state: StateFlow<OnboardingUiState> = combine(
        combine(step, myName, partnerName) { s, a, b -> Triple(s, a, b) },
        combine(startDate, myAvatar, partnerAvatar) { d, a, b -> Triple(d, a, b) },
    ) { (s, a, b), (d, av1, av2) ->
        OnboardingUiState(OnboardingStep.valueOf(s), a, b, d?.let(LocalDate::ofEpochDay), av1, av2)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, OnboardingUiState())

    fun setMyName(value: String) {
        savedStateHandle[KEY_MY_NAME] = value.take(CoupleSettings.NAME_MAX_LENGTH)
    }

    fun setPartnerName(value: String) {
        savedStateHandle[KEY_PARTNER_NAME] = value.take(CoupleSettings.NAME_MAX_LENGTH)
    }

    fun setStartDate(date: LocalDate) {
        if (date.isAfter(today)) return
        savedStateHandle[KEY_START] = date.toEpochDay()
    }

    fun pickMyAvatar(uri: Uri) = pickAvatar(uri, KEY_MY_AVATAR, "me")
    fun pickPartnerAvatar(uri: Uri) = pickAvatar(uri, KEY_PARTNER_AVATAR, "partner")

    private fun pickAvatar(uri: Uri, key: String, prefix: String) {
        viewModelScope.launch {
            val path = imageStorage.saveAvatar(uri, prefix) ?: return@launch
            imageStorage.delete(savedStateHandle.get<String?>(key))
            savedStateHandle[key] = path
        }
    }

    fun next() {
        val s = state.value
        val canGo = when (s.step) {
            OnboardingStep.NAMES -> s.namesValid
            OnboardingStep.START_DATE -> s.startDate != null
            else -> true
        }
        if (!canGo) return
        val nextStep = OnboardingStep.entries.getOrNull(s.step.ordinal + 1) ?: return
        savedStateHandle[KEY_STEP] = nextStep.name
    }

    fun back() {
        val prev = OnboardingStep.entries.getOrNull(state.value.step.ordinal - 1) ?: return
        savedStateHandle[KEY_STEP] = prev.name
    }

    fun complete() {
        val s = state.value
        val date = s.startDate ?: return
        if (!s.namesValid) return
        viewModelScope.launch {
            settingsRepository.completeOnboarding(s.myName, s.partnerName, date, s.myAvatarPath, s.partnerAvatarPath)
            val settings = settingsRepository.current()
            scheduler.reschedule(settings.reminderHour, settings.reminderMinute)
            scheduler.catchUpIfPastReminderTime(settings.reminderHour, settings.reminderMinute)
        }
    }

    private companion object {
        const val KEY_STEP = "step"
        const val KEY_MY_NAME = "my_name"
        const val KEY_PARTNER_NAME = "partner_name"
        const val KEY_START = "start_epoch_day"
        const val KEY_MY_AVATAR = "my_avatar"
        const val KEY_PARTNER_AVATAR = "partner_avatar"
    }
}
