package com.example.zerobook.ui.notes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zerobook.R
import com.example.zerobook.ui.components.LoadingState
import com.example.zerobook.ui.components.MessageState
import com.example.zerobook.ui.components.RatingBar

@Composable
fun BookNotesScreen(
    onBack: () -> Unit,
    viewModel: BookNotesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Lưu xong → tự quay lại màn Chi tiết
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }

    BookNotesContent(
        uiState = uiState,
        onBack = onBack,
        onRatingChange = viewModel::onRatingChange,
        onNoteChange = viewModel::onNoteChange,
        onSave = viewModel::save,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookNotesContent(
    uiState: BookNotesUiState,
    onBack: () -> Unit,
    onRatingChange: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notes_title)) },
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
        when {
            uiState.isLoading -> LoadingState(Modifier.padding(padding))

            uiState.notOnShelf -> MessageState(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = stringResource(R.string.notes_not_on_shelf),
                modifier = Modifier.padding(padding),
            )

            else -> Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .imePadding() // đẩy nội dung lên khi bàn phím mở
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                Text(uiState.title, style = MaterialTheme.typography.titleLarge)

                Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.notes_rating_label), style = MaterialTheme.typography.labelLarge)
                RatingBar(rating = uiState.rating, onRatingChange = onRatingChange)

                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = onNoteChange,
                    label = { Text(stringResource(R.string.notes_note_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 6,
                )

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onSave,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_save))
                }
            }
        }
    }
}
