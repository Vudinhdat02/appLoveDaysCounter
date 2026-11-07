package com.example.lovedays.ui.milestones

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.lovedays.core.time.ClockProvider
import com.example.lovedays.data.repository.SpecialDayRepository
import com.example.lovedays.domain.SpecialDay
import com.example.lovedays.ui.navigation.SpecialDayEditRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class SpecialDayForm(
    val title: String = "",
    val date: LocalDate,
    val repeatYearly: Boolean = true,
    val emoji: String = SpecialDay.DEFAULT_EMOJI,
    val remindEnabled: Boolean = true,
    val loaded: Boolean = false,
) {
    val canSave: Boolean get() = title.isNotBlank() && title.trim().length <= TITLE_MAX

    companion object {
        const val TITLE_MAX = 50
    }
}

@HiltViewModel
class SpecialDayEditViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: SpecialDayRepository,
    clock: ClockProvider,
) : ViewModel() {

    private val id: Long = runCatching { savedStateHandle.toRoute<SpecialDayEditRoute>().id }.getOrDefault(0L)

    private val title = savedStateHandle.getStateFlow(KEY_TITLE, "")
    private val date = savedStateHandle.getStateFlow(KEY_DATE, clock.today().toEpochDay())
    private val repeat = savedStateHandle.getStateFlow(KEY_REPEAT, true)
    private val emoji = savedStateHandle.getStateFlow(KEY_EMOJI, SpecialDay.DEFAULT_EMOJI)
    private val remind = savedStateHandle.getStateFlow(KEY_REMIND, true)
    private val loaded = savedStateHandle.getStateFlow(KEY_LOADED, id == 0L)

    private val _done = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val done: SharedFlow<Unit> = _done

    val form: StateFlow<SpecialDayForm> = combine(
        combine(title, date, repeat) { t, d, r -> Triple(t, d, r) },
        combine(emoji, remind, loaded) { e, rm, l -> Triple(e, rm, l) },
    ) { (t, d, r), (e, rm, l) ->
        SpecialDayForm(t, LocalDate.ofEpochDay(d), r, e, rm, l)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        SpecialDayForm(date = clock.today(), loaded = id == 0L),
    )

    init {
        if (id != 0L && savedStateHandle.get<Boolean>(KEY_LOADED) != true) {
            viewModelScope.launch {
                val day = repository.get(id)
                if (day == null) {
                    _done.tryEmit(Unit)
                    return@launch
                }
                savedStateHandle[KEY_TITLE] = day.title
                savedStateHandle[KEY_DATE] = day.date.toEpochDay()
                savedStateHandle[KEY_REPEAT] = day.repeatYearly
                savedStateHandle[KEY_EMOJI] = day.emoji
                savedStateHandle[KEY_REMIND] = day.remindEnabled
                savedStateHandle[KEY_LOADED] = true
            }
        }
    }

    fun setTitle(v: String) { savedStateHandle[KEY_TITLE] = v.take(SpecialDayForm.TITLE_MAX) }
    fun setDate(v: LocalDate) { savedStateHandle[KEY_DATE] = v.toEpochDay() }
    fun setRepeat(v: Boolean) { savedStateHandle[KEY_REPEAT] = v }
    fun setEmoji(v: String) { savedStateHandle[KEY_EMOJI] = v }
    fun setRemind(v: Boolean) { savedStateHandle[KEY_REMIND] = v }

    fun save() {
        val f = form.value
        if (!f.canSave) return
        viewModelScope.launch {
            val existing = if (id != 0L) repository.get(id) else null
            repository.save(
                SpecialDay(
                    id = id,
                    title = f.title.trim(),
                    date = f.date,
                    repeatYearly = f.repeatYearly,
                    emoji = f.emoji,
                    remindEnabled = f.remindEnabled,
                    createdAt = existing?.createdAt ?: 0,
                ),
            )
            _done.emit(Unit)
        }
    }

    fun delete() {
        if (id == 0L) return
        viewModelScope.launch {
            repository.delete(id)
            _done.emit(Unit)
        }
    }

    private companion object {
        const val KEY_TITLE = "title"
        const val KEY_DATE = "date"
        const val KEY_REPEAT = "repeat"
        const val KEY_EMOJI = "emoji"
        const val KEY_REMIND = "remind"
        const val KEY_LOADED = "loaded"
    }
}
