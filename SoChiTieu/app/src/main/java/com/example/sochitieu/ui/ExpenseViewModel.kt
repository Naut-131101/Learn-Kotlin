package com.example.sochitieu.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sochitieu.data.Category
import com.example.sochitieu.data.Expense
import com.example.sochitieu.data.ExpenseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

data class ExpenseUiState(
    val month: YearMonth = YearMonth.now(),
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
) {
    val total: Long get() = expenses.sumOf { it.amount }
}

class ExpenseViewModel(
    private val repository: ExpenseRepository = ExpenseRepository(),
) : ViewModel() {

    private val month = MutableStateFlow(YearMonth.now())

    /** Đổi tháng → flatMapLatest huỷ listener tháng cũ và mở listener tháng mới. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ExpenseUiState> = month
        .flatMapLatest { m ->
            repository.observeMonth(m)
                .map { list -> ExpenseUiState(month = m, expenses = list, isLoading = false) }
                .onStart { emit(ExpenseUiState(month = m, isLoading = true)) }
                .catch { e -> emit(ExpenseUiState(month = m, isLoading = false, error = e.message)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpenseUiState())

    /** Thông báo một lần (lỗi khi ghi) — UI hiện Snackbar rồi gọi onMessageShown(). */
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun previousMonth() = month.update { it.minusMonths(1) }
    fun nextMonth() = month.update { it.plusMonths(1) }

    /** editing = null → thêm mới, khác null → sửa khoản chi đó. */
    fun save(editing: Expense?, amount: Long, category: Category, note: String) = write {
        if (editing == null) {
            repository.add(Expense(amount = amount, category = category.name, note = note.trim()))
        } else {
            repository.update(editing.copy(amount = amount, category = category.name, note = note.trim()))
        }
    }

    fun delete(expense: Expense) = write { repository.delete(expense.id) }

    fun onMessageShown() {
        _message.value = null
    }

    private fun write(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                _message.value = "Không lưu được: ${e.message}"
            }
        }
    }
}