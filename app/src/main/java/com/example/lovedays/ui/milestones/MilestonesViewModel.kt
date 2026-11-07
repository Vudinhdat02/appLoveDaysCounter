package com.example.lovedays.ui.milestones

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lovedays.core.time.TodayProvider
import com.example.lovedays.data.datastore.SettingsRepository
import com.example.lovedays.data.repository.SpecialDayRepository
import com.example.lovedays.domain.Milestone
import com.example.lovedays.domain.MilestoneGenerator
import com.example.lovedays.domain.MilestoneType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class MilestoneFilter { ALL, AUTO, MINE }

data class MilestonesUiState(
    val filter: MilestoneFilter = MilestoneFilter.ALL,
    val items: List<Milestone> = emptyList(),
    val nextKey: String? = null,
    val loaded: Boolean = false,
)

@HiltViewModel
class MilestonesViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    specialDayRepository: SpecialDayRepository,
    todayProvider: TodayProvider,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val filter = savedStateHandle.getStateFlow(KEY_FILTER, MilestoneFilter.ALL.name)

    val state: StateFlow<MilestonesUiState> = combine(
        settingsRepository.settings,
        specialDayRepository.specialDays,
        todayProvider.today,
        filter,
    ) { settings, specials, today, filterName ->
        val f = MilestoneFilter.valueOf(filterName)
        val all = MilestoneGenerator.allMilestones(settings.startDate, specials, today, settings.countStartAsDayOne)
        val items = when (f) {
            MilestoneFilter.ALL -> all
            MilestoneFilter.AUTO -> all.filter { it.type != MilestoneType.CUSTOM }
            MilestoneFilter.MINE -> all.filter { it.type == MilestoneType.CUSTOM }
        }
        MilestonesUiState(f, items, MilestoneGenerator.nextUpcoming(items)?.key, loaded = true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MilestonesUiState())

    fun setFilter(f: MilestoneFilter) {
        savedStateHandle[KEY_FILTER] = f.name
    }

    private companion object {
        const val KEY_FILTER = "filter"
    }
}
