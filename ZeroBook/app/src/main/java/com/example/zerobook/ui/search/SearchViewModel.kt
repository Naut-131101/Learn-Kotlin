package com.example.zerobook.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerobook.data.repository.BookRepository
import com.example.zerobook.data.repository.ShelfRepository
import com.example.zerobook.model.Book
import com.example.zerobook.model.ReadingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Sealed interface: mỗi trạng thái là một kiểu riêng → `when` phải xử lý đủ mọi trường hợp. */
sealed interface SearchResultState {
    data object Idle : SearchResultState
    data object Loading : SearchResultState
    data object Empty : SearchResultState
    data object Error : SearchResultState
    data class Success(val books: List<Book>) : SearchResultState
}

data class SearchUiState(
    val query: String = "",
    val result: SearchResultState = SearchResultState.Idle,
    /** bookId → trạng thái, để đánh dấu sách đã có trên kệ ngay trong kết quả tìm kiếm. */
    val shelfStatuses: Map<String, ReadingStatus> = emptyMap(),
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    shelfRepository: ShelfRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val result = MutableStateFlow<SearchResultState>(SearchResultState.Idle)
    private var searchJob: Job? = null

    val uiState: StateFlow<SearchUiState> = combine(
        query,
        result,
        shelfRepository.observeShelf(),
    ) { currentQuery, currentResult, shelf ->
        SearchUiState(
            query = currentQuery,
            result = currentResult,
            shelfStatuses = shelf.associate { it.book.id to it.status },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onSearch() {
        val trimmed = query.value.trim()
        if (trimmed.isEmpty()) return

        // Huỷ lượt tìm cũ nếu người dùng tìm lại trước khi kết quả cũ về
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            result.value = SearchResultState.Loading
            result.value = bookRepository.search(trimmed).fold(
                onSuccess = { books ->
                    if (books.isEmpty()) SearchResultState.Empty else SearchResultState.Success(books)
                },
                onFailure = { SearchResultState.Error },
            )
        }
    }

    fun onClear() {
        searchJob?.cancel()
        query.value = ""
        result.value = SearchResultState.Idle
    }
}
