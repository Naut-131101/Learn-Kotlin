package com.example.zerobook.ui.shelf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerobook.data.repository.ShelfRepository
import com.example.zerobook.model.ReadingStatus
import com.example.zerobook.model.ShelfBook
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ShelfUiState(
    val isLoading: Boolean = true,
    val selectedStatus: ReadingStatus = ReadingStatus.READING,
    val books: List<ShelfBook> = emptyList(),
    val counts: Map<ReadingStatus, Int> = emptyMap(),
)

@HiltViewModel
class ShelfViewModel @Inject constructor(
    shelfRepository: ShelfRepository,
) : ViewModel() {

    private val selectedStatus = MutableStateFlow(ReadingStatus.READING)

    /**
     * combine: mỗi khi kệ sách (Room) hoặc tab đang chọn thay đổi → tính lại UI state.
     * WhileSubscribed(5_000): ngừng lắng nghe Room 5 giây sau khi màn hình ẩn đi
     * (giữ 5 giây để xoay màn hình không phải tải lại).
     */
    val uiState: StateFlow<ShelfUiState> = combine(
        shelfRepository.observeShelf(),
        selectedStatus,
    ) { allBooks, status ->
        ShelfUiState(
            isLoading = false,
            selectedStatus = status,
            books = allBooks.filter { it.status == status },
            counts = allBooks.groupingBy { it.status }.eachCount(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShelfUiState())

    fun onStatusSelected(status: ReadingStatus) {
        selectedStatus.value = status
    }
}
