package com.example.sochitieu.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sochitieu.data.Category
import com.example.sochitieu.data.Expense
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

private val vnd = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"))
private fun Long.toVnd() = "${vnd.format(this)} đ"
private val dayFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(viewModel: ExpenseViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    // null = đóng hộp thoại; Expense() mới = thêm; Expense có id = sửa
    var editing by remember { mutableStateOf<Expense?>(null) }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sổ chi tiêu") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = Expense() }) {
                Icon(Icons.Filled.Add, contentDescription = "Thêm khoản chi")
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            MonthHeader(state, onPrevious = viewModel::previousMonth, onNext = viewModel::nextMonth)
            HorizontalDivider()
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
                    Text("Lỗi: ${state.error}", color = MaterialTheme.colorScheme.error)
                }
                state.expenses.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("Tháng này chưa có khoản chi nào")
                }
                else -> LazyColumn {
                    items(state.expenses, key = { it.id }) { expense ->
                        ExpenseRow(
                            expense = expense,
                            onClick = { editing = expense },
                            onDelete = { viewModel.delete(expense) },
                        )
                    }
                }
            }
        }
    }

    editing?.let { expense ->
        ExpenseDialog(
            initial = expense,
            onDismiss = { editing = null },
            onSave = { amount, category, note ->
                viewModel.save(expense.takeIf { it.id.isNotEmpty() }, amount, category, note)
                editing = null
            },
        )
    }
}

@Composable
private fun MonthHeader(state: ExpenseUiState, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Tháng trước")
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Tháng ${state.month.monthValue}/${state.month.year}", style = MaterialTheme.typography.titleMedium)
            Text("Tổng chi: ${state.total.toVnd()}", style = MaterialTheme.typography.bodyMedium)
        }
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Tháng sau")
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense, onClick: () -> Unit, onDelete: () -> Unit) {
    val category = Category.entries.find { it.name == expense.category } ?: Category.OTHER
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(expense.note.ifBlank { category.label }) },
        supportingContent = { Text("${category.label} • ${dayFormat.format(expense.spentAt.toDate())}") },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(expense.amount.toVnd())
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Xoá")
                }
            }
        },
    )
}

@Composable
private fun ExpenseDialog(
    initial: Expense,
    onDismiss: () -> Unit,
    onSave: (amount: Long, category: Category, note: String) -> Unit,
) {
    var amountText by rememberSaveable { mutableStateOf(if (initial.amount > 0) initial.amount.toString() else "") }
    var category by rememberSaveable {
        // Thêm mới: mặc định Ăn uống. Sửa: lấy danh mục đang lưu
        mutableStateOf(
            if (initial.id.isEmpty()) Category.FOOD
            else Category.entries.find { it.name == initial.category } ?: Category.OTHER,
        )
    }
    var note by rememberSaveable { mutableStateOf(initial.note) }
    val amount = amountText.toLongOrNull() ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id.isEmpty()) "Thêm khoản chi" else "Sửa khoản chi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input -> amountText = input.filter(Char::isDigit).take(10) },
                    label = { Text("Số tiền (đ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Category.entries.forEach { c ->
                        FilterChip(selected = c == category, onClick = { category = c }, label = { Text(c.label) })
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(200) },
                    label = { Text("Ghi chú") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(amount, category, note) }, enabled = amount > 0) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Huỷ") } },
    )
}