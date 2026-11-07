package com.example.lovedays.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lovedays.core.time.TodayProvider
import com.example.lovedays.data.datastore.CoupleSettings
import com.example.lovedays.data.datastore.SettingsRepository
import com.example.lovedays.data.repository.SpecialDayRepository
import com.example.lovedays.domain.CounterUnit
import com.example.lovedays.domain.DayCounter
import com.example.lovedays.domain.Milestone
import com.example.lovedays.domain.MilestoneGenerator
import com.example.lovedays.domain.SpecialDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject

data class HomeUiState(
    val myName: String,
    val partnerName: String,
    val myAvatarPath: String?,
    val partnerAvatarPath: String?,
    val startDate: LocalDate,
    val displayedDays: Long,
    val period: Period,
    val unit: CounterUnit,
    val counterValue: Long,
    val nextMilestone: Milestone?,
) {
    companion object {
        fun from(
            settings: CoupleSettings,
            specialDays: List<SpecialDay>,
            today: LocalDate,
            unit: CounterUnit,
        ): HomeUiState? {
            val start = settings.startDate ?: return null
            val dayOne = settings.countStartAsDayOne
            val milestones = MilestoneGenerator.allMilestones(start, specialDays, today, dayOne)
            return HomeUiState(
                myName = settings.myName,
                partnerName = settings.partnerName,
                myAvatarPath = settings.myAvatarPath,
                partnerAvatarPath = settings.partnerAvatarPath,
                startDate = start,
                displayedDays = DayCounter.displayedDays(start, today, dayOne),
                period = DayCounter.period(start, today),
                unit = unit,
                counterValue = DayCounter.valueIn(unit, start, today, dayOne),
                nextMilestone = MilestoneGenerator.nextUpcoming(milestones),
            )
        }
    }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    specialDayRepository: SpecialDayRepository,
    todayProvider: TodayProvider,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val unit = savedStateHandle.getStateFlow(KEY_UNIT, CounterUnit.DAYS.name)

    val state: StateFlow<HomeUiState?> = combine(
        settingsRepository.settings,
        specialDayRepository.specialDays,
        todayProvider.today,
        unit,
    ) { settings, specials, today, unitName ->
        HomeUiState.from(settings, specials, today, CounterUnit.valueOf(unitName))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun cycleUnit() {
        val current = CounterUnit.valueOf(unit.value)
        savedStateHandle[KEY_UNIT] = current.next().name
    }

    private companion object {
        const val KEY_UNIT = "counter_unit"
    }
}
