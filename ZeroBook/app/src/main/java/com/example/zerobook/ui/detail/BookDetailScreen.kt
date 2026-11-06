package com.example.zerobook.ui.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zerobook.R
import com.example.zerobook.model.Book
import com.example.zerobook.model.ReadingStatus
import com.example.zerobook.model.ShelfBook
import com.example.zerobook.ui.components.BookCover
import com.example.zerobook.ui.components.LoadingState
import com.example.zerobook.ui.components.MessageState
import com.example.zerobook.ui.components.RatingBar
import com.example.zerobook.ui.components.labelRes

@Composable
fun BookDetailScreen(
    onBack: () -> Unit,
    onOpenNotes: (String) -> Unit,
    viewModel: BookDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BookDetailContent(
        uiState = uiState,
        onBack = onBack,
        onStatusSelected = viewModel::onStatusSelected,
        onRemoveFromShelf = viewModel::onRemoveFromShelf,
        onOpenNotes = onOpenNotes,
        onRetry = viewModel::retry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailContent(
    uiState: BookDetailUiState,
    onBack: () -> Unit,
    onStatusSelected: (ReadingStatus) -> Unit,
    onRemoveFromShelf: () -> Unit,
    onOpenNotes: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        val book = uiState.book
        when {
            uiState.isLoading -> LoadingState(Modifier.padding(padding))

            book == null -> MessageState(
                icon = Icons.Filled.CloudOff,
                title = stringResource(R.string.error_network_title),
                message = stringResource(R.string.error_network_message),
                actionLabel = stringResource(R.string.action_retry),
                onAction = onRetry,
                modifier = Modifier.padding(padding),
            )

            else -> BookDetailBody(
                book = book,
                shelfBook = uiState.shelfBook,
                onStatusSelected = onStatusSelected,
                onRemoveFromShelf = onRemoveFromShelf,
                onOpenNotes = { onOpenNotes(book.id) },
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookDetailBody(
    book: Book,
    shelfBook: ShelfBook?,
    onStatusSelected: (ReadingStatus) -> Unit,
    onRemoveFromShelf: () -> Unit,
    onOpenNotes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // rememberSaveable: hộp thoại vẫn mở sau khi xoay màn hình
    var showRemoveDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row {
            BookCover(
                imageUrl = book.thumbnailUrl,
                contentDescription = stringResource(R.string.cd_book_cover, book.title),
                modifier = Modifier.size(width = 110.dp, height = 165.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(book.title, style = MaterialTheme.typography.titleLarge)
                if (book.authors.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = book.authors.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val meta = listOfNotNull(book.publisher, book.publishedDate?.take(4))
                    .joinToString(" · ")
                if (meta.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(meta, style = MaterialTheme.typography.bodySmall)
                }
                book.pageCount?.let { pages ->
                    Text(
                        text = stringResource(R.string.detail_pages, pages),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.detail_status_label), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        val statuses = ReadingStatus.entries
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            statuses.forEachIndexed { index, status ->
                SegmentedButton(
                    selected = shelfBook?.status == status,
                    onClick = { onStatusSelected(status) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = statuses.size),
                ) {
                    Text(stringResource(status.labelRes()), maxLines = 1)
                }
            }
        }

        // animateContentSize: khi thêm sách lên kệ, phần nút và đánh giá
        // giãn ra mượt mà thay vì nhảy đột ngột.
        Column(
            Modifier
                .fillMaxWidth()
                .animateContentSize(),
        ) {
            if (shelfBook == null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.detail_not_on_shelf),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                if (shelfBook.rating > 0) {
                    Spacer(Modifier.height(16.dp))
                    RatingBar(rating = shelfBook.rating, onRatingChange = null, starSize = 20.dp)
                }
                if (shelfBook.note.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(shelfBook.note, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(onClick = onOpenNotes) {
                        Icon(Icons.Filled.EditNote, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.action_open_notes))
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { showRemoveDialog = true }) {
                        Text(stringResource(R.string.action_remove_from_shelf))
                    }
                }
            }
        }

        book.description?.let { description ->
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.detail_description), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium)
        }
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text(stringResource(R.string.remove_dialog_title)) },
            text = { Text(stringResource(R.string.remove_dialog_message, book.title)) },
            confirmButton = {
                TextButton(onClick = {
                    showRemoveDialog = false
                    onRemoveFromShelf()
                }) { Text(stringResource(R.string.action_remove)) }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}
