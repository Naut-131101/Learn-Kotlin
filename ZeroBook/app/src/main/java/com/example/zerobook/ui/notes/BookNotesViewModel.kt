package com.example.zerobook.ui.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.zerobook.data.repository.ShelfRepository
import com.example.zerobook.ui.navigation.BookNotesRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookNotesUiState(
    val isLoading: Boolean = true,
    val notOnShelf: Boolean = false,
    val title: String = "",
    val rating: Int = 0,
    val note: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
)

@HiltViewModel
class BookNotesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val shelfRepository: ShelfRepository,
) : ViewModel() {

    private val bookId: String = savedStateHandle.toRoute<BookNotesRoute>().bookId

    private val _uiState = MutableStateFlow(BookNotesUiState())
    val uiState: StateFlow<BookNotesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // first(): chỉ đọc 1 lần để điền form. Không lắng nghe liên tục,
            // nếu không, dữ liệu đồng bộ về sẽ ghi đè chữ người dùng đang gõ.
            val shelfBook = shelfRepository.observeShelfBook(bookId).first()
            _uiState.value = if (shelfBook == null) {
                BookNotesUiState(isLoading = false, notOnShelf = true)
            } else {
                BookNotesUiState(
                    isLoading = false,
                    title = shelfBook.book.title,
                    rating = shelfBook.rating,
                    note = shelfBook.note,
                )
            }
        }
    }

    /** Bấm lại đúng ngôi sao đang chọn → bỏ đánh giá (về 0). */
    fun onRatingChange(rating: Int) {
        _uiState.update { it.copy(rating = if (it.rating == rating) 0 else rating) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving || state.notOnShelf) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            shelfRepository.updateReview(bookId, state.rating, state.note.trim())
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }
}
