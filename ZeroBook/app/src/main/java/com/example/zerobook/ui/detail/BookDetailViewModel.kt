package com.example.zerobook.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.zerobook.data.repository.BookRepository
import com.example.zerobook.data.repository.ShelfRepository
import com.example.zerobook.model.Book
import com.example.zerobook.model.ReadingStatus
import com.example.zerobook.model.ShelfBook
import com.example.zerobook.ui.navigation.BookDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookDetailUiState(
    val isLoading: Boolean = true,
    val book: Book? = null,
    val shelfBook: ShelfBook? = null,
    val isError: Boolean = false,
)

private sealed interface RemoteBook {
    data object Loading : RemoteBook
    data class Loaded(val book: Book) : RemoteBook
    data object Failed : RemoteBook
}

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository,
    private val shelfRepository: ShelfRepository,
) : ViewModel() {

    /** Lấy tham số từ route type-safe: đúng kiểu, không cần ép kiểu bằng tay. */
    private val bookId: String = savedStateHandle.toRoute<BookDetailRoute>().bookId

    private val remoteBook = MutableStateFlow<RemoteBook>(RemoteBook.Loading)

    /**
     * Ưu tiên dữ liệu mới nhất từ API; nếu mất mạng thì dùng bản đã lưu trong Room (kệ sách).
     * Nhờ vậy sách trên kệ vẫn xem được khi offline.
     */
    val uiState: StateFlow<BookDetailUiState> = combine(
        remoteBook,
        shelfRepository.observeShelfBook(bookId),
    ) { remote, shelfBook ->
        val book = (remote as? RemoteBook.Loaded)?.book ?: shelfBook?.book
        BookDetailUiState(
            isLoading = remote is RemoteBook.Loading && book == null,
            book = book,
            shelfBook = shelfBook,
            isError = remote is RemoteBook.Failed && book == null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BookDetailUiState())

    init {
        loadBook()
    }

    fun retry() = loadBook()

    private fun loadBook() {
        viewModelScope.launch {
            remoteBook.value = RemoteBook.Loading
            remoteBook.value = bookRepository.getBook(bookId).fold(
                onSuccess = { RemoteBook.Loaded(it) },
                onFailure = { RemoteBook.Failed },
            )
        }
    }

    fun onStatusSelected(status: ReadingStatus) {
        val book = uiState.value.book ?: return
        viewModelScope.launch { shelfRepository.addOrUpdateStatus(book, status) }
    }

    fun onRemoveFromShelf() {
        viewModelScope.launch { shelfRepository.remove(bookId) }
    }
}
