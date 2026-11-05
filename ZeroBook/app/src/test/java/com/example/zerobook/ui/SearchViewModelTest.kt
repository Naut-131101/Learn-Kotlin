package com.example.zerobook.ui

import com.example.zerobook.MainDispatcherRule
import com.example.zerobook.fakes.FakeBookRepository
import com.example.zerobook.fakes.FakeShelfRepository
import com.example.zerobook.fakes.testBook
import com.example.zerobook.fakes.testShelfBook
import com.example.zerobook.model.ReadingStatus
import com.example.zerobook.ui.search.SearchResultState
import com.example.zerobook.ui.search.SearchViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val bookRepository = FakeBookRepository()
    private val shelfRepository = FakeShelfRepository()

    private fun TestScope.createViewModel(): SearchViewModel {
        val viewModel = SearchViewModel(bookRepository, shelfRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    @Test
    fun search_success_showsBooks() = runTest {
        bookRepository.searchResult = Result.success(listOf(testBook("a"), testBook("b")))
        val viewModel = createViewModel()

        viewModel.onQueryChange("  kotlin  ")
        viewModel.onSearch()

        assertEquals("kotlin", bookRepository.lastQuery) // đã bỏ khoảng trắng thừa
        val result = viewModel.uiState.value.result
        assertTrue(result is SearchResultState.Success)
        assertEquals(listOf("a", "b"), (result as SearchResultState.Success).books.map { it.id })
    }

    @Test
    fun search_noResults_showsEmpty() = runTest {
        bookRepository.searchResult = Result.success(emptyList())
        val viewModel = createViewModel()

        viewModel.onQueryChange("xyz")
        viewModel.onSearch()

        assertEquals(SearchResultState.Empty, viewModel.uiState.value.result)
    }

    @Test
    fun search_networkFailure_showsError() = runTest {
        bookRepository.searchResult = Result.failure(IOException("offline"))
        val viewModel = createViewModel()

        viewModel.onQueryChange("android")
        viewModel.onSearch()

        assertEquals(SearchResultState.Error, viewModel.uiState.value.result)
    }

    @Test
    fun blankQuery_doesNotCallApi() = runTest {
        val viewModel = createViewModel()

        viewModel.onQueryChange("   ")
        viewModel.onSearch()

        assertNull(bookRepository.lastQuery)
        assertEquals(SearchResultState.Idle, viewModel.uiState.value.result)
    }

    @Test
    fun booksAlreadyOnShelf_areMarkedWithTheirStatus() = runTest {
        shelfRepository.books.value = listOf(testShelfBook("a", ReadingStatus.READ))
        val viewModel = createViewModel()

        assertEquals(ReadingStatus.READ, viewModel.uiState.value.shelfStatuses["a"])
    }
}
