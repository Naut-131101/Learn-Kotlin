package com.example.zerobook.ui

import com.example.zerobook.MainDispatcherRule
import com.example.zerobook.fakes.FakeShelfRepository
import com.example.zerobook.fakes.testShelfBook
import com.example.zerobook.model.ReadingStatus
import com.example.zerobook.ui.shelf.ShelfViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShelfViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeShelfRepository()

    @Test
    fun defaultTab_showsOnlyReadingBooks_andCountsAllStatuses() = runTest {
        repository.books.value = listOf(
            testShelfBook("1", ReadingStatus.READING),
            testShelfBook("2", ReadingStatus.READ),
            testShelfBook("3", ReadingStatus.WANT_TO_READ),
            testShelfBook("4", ReadingStatus.READING),
        )
        val viewModel = ShelfViewModel(repository)
        // stateIn(WhileSubscribed) chỉ chạy khi có người thu thập → giả lập UI đang quan sát
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(ReadingStatus.READING, state.selectedStatus)
        assertEquals(setOf("1", "4"), state.books.map { it.book.id }.toSet())
        assertEquals(2, state.counts[ReadingStatus.READING])
        assertEquals(1, state.counts[ReadingStatus.READ])
        assertEquals(1, state.counts[ReadingStatus.WANT_TO_READ])
    }

    @Test
    fun selectingStatus_filtersBooks() = runTest {
        repository.books.value = listOf(
            testShelfBook("1", ReadingStatus.READING),
            testShelfBook("2", ReadingStatus.READ),
        )
        val viewModel = ShelfViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.onStatusSelected(ReadingStatus.READ)

        val state = viewModel.uiState.value
        assertEquals(ReadingStatus.READ, state.selectedStatus)
        assertEquals(listOf("2"), state.books.map { it.book.id })
    }

    @Test
    fun shelfUpdates_areReflectedInUiState() = runTest {
        val viewModel = ShelfViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        assertTrue(viewModel.uiState.value.books.isEmpty())

        repository.books.value = listOf(testShelfBook("9", ReadingStatus.READING))

        assertEquals(listOf("9"), viewModel.uiState.value.books.map { it.book.id })
    }
}
