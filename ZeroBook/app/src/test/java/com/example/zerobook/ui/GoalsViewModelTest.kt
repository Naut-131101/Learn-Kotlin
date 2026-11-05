package com.example.zerobook.ui

import com.example.zerobook.MainDispatcherRule
import com.example.zerobook.fakes.FakeShelfRepository
import com.example.zerobook.fakes.FakeUserPreferencesRepository
import com.example.zerobook.fakes.testShelfBook
import com.example.zerobook.model.ReadingStatus
import com.example.zerobook.ui.goals.GoalsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class GoalsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val shelfRepository = FakeShelfRepository()
    private val preferencesRepository = FakeUserPreferencesRepository()

    @Test
    fun progress_countsOnlyBooksFinishedThisYear() = runTest {
        val now = System.currentTimeMillis()
        val moreThanAYearAgo = now - TimeUnit.DAYS.toMillis(400)
        preferencesRepository.prefs.value = preferencesRepository.prefs.value.copy(yearlyGoal = 10)
        shelfRepository.books.value = listOf(
            testShelfBook("1", ReadingStatus.READ, finishedAt = now, pageCount = 300),
            testShelfBook("2", ReadingStatus.READ, finishedAt = now, pageCount = 150),
            testShelfBook("3", ReadingStatus.READ, finishedAt = moreThanAYearAgo), // năm trước → không tính
            testShelfBook("4", ReadingStatus.READING),
            testShelfBook("5", ReadingStatus.WANT_TO_READ),
        )

        val viewModel = GoalsViewModel(shelfRepository, preferencesRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        val state = viewModel.uiState.value
        assertEquals(10, state.goal)
        assertEquals(2, state.readThisYear)
        assertEquals(0.2f, state.progress, 0.0001f)
        assertEquals(450, state.pagesThisYear)
        assertEquals(1, state.readingCount)
        assertEquals(1, state.wantToReadCount)
    }

    @Test
    fun setGoal_updatesGoal_andIgnoresInvalidValues() = runTest {
        val viewModel = GoalsViewModel(shelfRepository, preferencesRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.setGoal(24)
        assertEquals(24, viewModel.uiState.value.goal)

        viewModel.setGoal(0) // không hợp lệ → giữ nguyên
        assertEquals(24, viewModel.uiState.value.goal)
    }

    @Test
    fun progress_isCappedAtOneHundredPercent() = runTest {
        val now = System.currentTimeMillis()
        preferencesRepository.prefs.value = preferencesRepository.prefs.value.copy(yearlyGoal = 1)
        shelfRepository.books.value = listOf(
            testShelfBook("1", ReadingStatus.READ, finishedAt = now),
            testShelfBook("2", ReadingStatus.READ, finishedAt = now),
        )

        val viewModel = GoalsViewModel(shelfRepository, preferencesRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        assertEquals(1f, viewModel.uiState.value.progress, 0.0001f)
    }
}
