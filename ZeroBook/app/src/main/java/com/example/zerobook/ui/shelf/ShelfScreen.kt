package com.example.zerobook.ui.shelf

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zerobook.R
import com.example.zerobook.model.ReadingStatus
import com.example.zerobook.ui.components.BookListItem
import com.example.zerobook.ui.components.LoadingState
import com.example.zerobook.ui.components.MessageState
import com.example.zerobook.ui.components.RatingBar
import com.example.zerobook.ui.components.labelRes

/** Stateful: lấy ViewModel, thu thập state, rồi truyền xuống ShelfContent. */
@Composable
fun ShelfScreen(
    onBookClick: (String) -> Unit,
    onFindBooksClick: () -> Unit,
    viewModel: ShelfViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ShelfContent(
        uiState = uiState,
        onStatusSelected = viewModel::onStatusSelected,
        onBookClick = onBookClick,
        onFindBooksClick = onFindBooksClick,
    )
}

/** Stateless: chỉ nhận state + lambda → dễ viết Compose UI test và Preview. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelfContent(
    uiState: ShelfUiState,
    onStatusSelected: (ReadingStatus) -> Unit,
    onBookClick: (String) -> Unit,
    onFindBooksClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statuses = ReadingStatus.entries

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_shelf)) }) },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            PrimaryTabRow(selectedTabIndex = statuses.indexOf(uiState.selectedStatus)) {
                statuses.forEach { status ->
                    Tab(
                        selected = status == uiState.selectedStatus,
                        onClick = { onStatusSelected(status) },
                        text = {
                            Text(
                                text = stringResource(
                                    R.string.shelf_tab_with_count,
                                    stringResource(status.labelRes()),
                                    uiState.counts[status] ?: 0,
                                ),
                                maxLines = 1,
                            )
                        },
                    )
                }
            }

            when {
                uiState.isLoading -> LoadingState()

                uiState.books.isEmpty() -> MessageState(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = stringResource(R.string.shelf_empty_title),
                    message = stringResource(R.string.shelf_empty_message),
                    actionLabel = stringResource(R.string.shelf_empty_action),
                    onAction = onFindBooksClick,
                )

                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(uiState.books, key = { it.book.id }) { shelfBook ->
                        BookListItem(
                            book = shelfBook.book,
                            onClick = { onBookClick(shelfBook.book.id) },
                            supportingContent = if (shelfBook.rating > 0) {
                                {
                                    RatingBar(
                                        rating = shelfBook.rating,
                                        onRatingChange = null,
                                        starSize = 16.dp,
                                    )
                                }
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }
    }
}
