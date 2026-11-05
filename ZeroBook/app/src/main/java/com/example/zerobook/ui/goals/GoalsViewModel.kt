package com.example.zerobook.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerobook.data.repository.ShelfRepository
import com.example.zerobook.data.repository.UserPreferencesRepository
import com.example.zerobook.model.ReadingStatus
import com.example.zerobook.model.ShelfBook
import com.example.zerobook.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.Year
import java.time.ZoneId
import javax.inject.Inject

data class GoalsUiState(
    val isLoading: Boolean = true,
    val year: Int = Year.now().value,
    val goal: Int = UserPreferences.DEFAULT_YEARLY_GOAL,
    val readThisYear: Int = 0,
    val readingCount: Int = 0,
    val wantToReadCount: Int = 0,
    val pagesThisYear: Int = 0,
    val finishedThisYear: List<ShelfBook> = emptyList(),
) {
    /** Tỉ lệ hoàn thành 0..1 cho thanh tiến độ. */
    val progress: Float
        get() = if (goal <= 0) 0f else (readThisYear.toFloat() / goal).coerceIn(0f, 1f)
}

@HiltViewModel
class GoalsViewModel @Inject constructor(
    shelfRepository: ShelfRepository,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<GoalsUiState> = combine(
        preferencesRepository.preferences,
        shelfRepository.observeShelf(),
    ) { prefs, books ->
        val zone = ZoneId.systemDefault()
        val year = Year.now(zone).value
        val finished = books
            .filter { book ->
                val finishedAt = book.finishedAt
                book.status == ReadingStatus.READ &&
                    finishedAt != null &&
                    Instant.ofEpochMilli(finishedAt).atZone(zone).year == year
            }
            .sortedByDescending { it.finishedAt }

        GoalsUiState(
            isLoading = false,
            year = year,
            goal = prefs.yearlyGoal,
            readThisYear = finished.size,
            readingCount = books.count { it.status == ReadingStatus.READING },
            wantToReadCount = books.count { it.status == ReadingStatus.WANT_TO_READ },
            pagesThisYear = finished.sumOf { it.book.pageCount ?: 0 },
            finishedThisYear = finished,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalsUiState())

    fun setGoal(goal: Int) {
        if (goal !in GOAL_RANGE) return
        viewModelScope.launch { preferencesRepository.setYearlyGoal(goal) }
    }

    companion object {
        val GOAL_RANGE = 1..365
    }
}
