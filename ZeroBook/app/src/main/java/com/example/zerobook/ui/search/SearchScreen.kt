package com.example.zerobook.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zerobook.R
import com.example.zerobook.ui.components.BookListItem
import com.example.zerobook.ui.components.LoadingState
import com.example.zerobook.ui.components.MessageState
import com.example.zerobook.ui.components.labelRes

@Composable
fun SearchScreen(
    onBookClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SearchContent(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::onSearch,
        onClear = viewModel::onClear,
        onBookClick = onBookClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_search)) }) },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = onClear) {
                            Icon(Icons.Filled.Close, stringResource(R.string.action_clear))
                        }
                    }
                },
                singleLine = true,
                // Nút Enter trên bàn phím thành nút "Tìm kiếm"
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus() // ẩn bàn phím
                        onSearch()
                    },
                ),
            )

            when (val result = uiState.result) {
                SearchResultState.Idle -> MessageState(
                    icon = Icons.Filled.Search,
                    title = stringResource(R.string.search_idle_title),
                    message = stringResource(R.string.search_idle_message),
                )

                SearchResultState.Loading -> LoadingState()

                SearchResultState.Empty -> MessageState(
                    icon = Icons.Filled.SearchOff,
                    title = stringResource(R.string.search_empty_title),
                    message = stringResource(R.string.search_empty_message),
                )

                SearchResultState.Error -> MessageState(
                    icon = Icons.Filled.CloudOff,
                    title = stringResource(R.string.error_network_title),
                    message = stringResource(R.string.error_network_message),
                    actionLabel = stringResource(R.string.action_retry),
                    onAction = onSearch,
                )

                is SearchResultState.Success -> LazyColumn(Modifier.fillMaxSize()) {
                    items(result.books, key = { it.id }) { book ->
                        val shelfStatus = uiState.shelfStatuses[book.id]
                        BookListItem(
                            book = book,
                            onClick = { onBookClick(book.id) },
                            supportingContent = if (shelfStatus != null) {
                                {
                                    Text(
                                        text = stringResource(
                                            R.string.search_on_shelf,
                                            stringResource(shelfStatus.labelRes()),
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
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
