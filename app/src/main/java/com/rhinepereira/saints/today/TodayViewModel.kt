package com.rhinepereira.saints.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhinepereira.saints.data.bible.DayReadings
import com.rhinepereira.saints.data.bible.ReadingsRepository
import com.rhinepereira.saints.data.calendar.LiturgicalCalendar
import com.rhinepereira.saints.data.calendar.LiturgicalDay
import com.rhinepereira.saints.data.calendar.SeasonSpan
import com.rhinepereira.saints.data.model.UiError
import com.rhinepereira.saints.data.nameday.NameDay
import com.rhinepereira.saints.data.nameday.NameDayStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

sealed interface ReadingsUi {
    data object Loading : ReadingsUi
    data object None : ReadingsUi
    data class Ready(val readings: DayReadings) : ReadingsUi
    data class Failed(val error: UiError) : ReadingsUi
}

data class TodayState(
    val today: LocalDate = LocalDate.now(),
    val selected: LocalDate = today,
    val day: LiturgicalDay? = null,
    val spans: List<SeasonSpan> = emptyList(),
    val nextSeason: LiturgicalDay? = null,
    val nameDays: List<NameDay> = emptyList(),
    val readings: ReadingsUi = ReadingsUi.Loading
)

class TodayViewModel(
    private val calendar: LiturgicalCalendar,
    private val readingsRepo: ReadingsRepository,
    private val nameDayStore: NameDayStore
) : ViewModel() {

    private val _state = MutableStateFlow(TodayState())
    val state: StateFlow<TodayState> = _state.asStateFlow()
    private var readingsJob: Job? = null

    init {
        select(_state.value.today, immediate = true)
        viewModelScope.launch {
            nameDayStore.all.collect { _ ->
                _state.update { it.copy(nameDays = nameDayStore.on(it.selected)) }
            }
        }
    }

    fun select(date: LocalDate, immediate: Boolean = false) {
        val years = calendar.availableYears
        val target = when {
            years.isEmpty() -> date
            date.year < years.first -> LocalDate.of(years.first, 1, 1)
            date.year > years.last -> LocalDate.of(years.last, 12, 31)
            else -> date
        }
        _state.update { it.copy(selected = target, nameDays = nameDayStore.on(target)) }
        viewModelScope.launch {
            val (day, spans, next) = withContext(Dispatchers.Default) {
                Triple(calendar.day(target), calendar.spans(target.year), calendar.nextSeason(target))
            }
            if (_state.value.selected == target) {
                _state.update { it.copy(day = day, spans = spans, nextSeason = next) }
            }
        }
        loadReadings(target, immediate)
    }

    fun shift(days: Long) = select(_state.value.selected.plusDays(days), immediate = true)

    fun backToToday() = select(LocalDate.now(), immediate = true)

    fun retryReadings() = loadReadings(_state.value.selected, immediate = true)

    private fun loadReadings(date: LocalDate, immediate: Boolean) {
        readingsJob?.cancel()
        readingsJob = viewModelScope.launch {
            _state.update { it.copy(readings = ReadingsUi.Loading) }
            if (!immediate) delay(300) // the wheel scrubs many days a second; fetch where it settles
            val ui = try {
                readingsRepo.readings(date)?.let { ReadingsUi.Ready(it) } ?: ReadingsUi.None
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ReadingsUi.Failed(UiError.from(e))
            }
            _state.update { it.copy(readings = ui) }
        }
    }
}
