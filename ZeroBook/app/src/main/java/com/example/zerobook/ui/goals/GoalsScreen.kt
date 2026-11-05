package com.example.zerobook.ui.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zerobook.R
import com.example.zerobook.ui.components.BookListItem
import com.example.zerobook.ui.components.LoadingState

@Composable
fun GoalsScreen(
    onBookClick: (String) -> Unit,
    viewModel: GoalsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    GoalsContent(
        uiState = uiState,
        onGoalChange = viewModel::setGoal,
        onBookClick = onBookClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsContent(
    uiState: GoalsUiState,
    onGoalChange: (Int) -> Unit,
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showGoalDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_goals)) }) },
    ) { padding ->
        if (uiState.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                GoalProgressCard(
                    uiState = uiState,
                    onEditGoal = { showGoalDialog = true },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        value = uiState.readingCount.toString(),
                        label = stringResource(R.string.goals_stat_reading),
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        value = uiState.wantToReadCount.toString(),
                        label = stringResource(R.string.goals_stat_want),
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        value = uiState.pagesThisYear.toString(),
                        label = stringResource(R.string.goals_stat_pages),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.goals_finished_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            if (uiState.finishedThisYear.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.goals_finished_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            } else {
                items(uiState.finishedThisYear, key = { it.book.id }) { shelfBook ->
                    BookListItem(
                        book = shelfBook.book,
                        onClick = { onBookClick(shelfBook.book.id) },
                    )
                }
            }
        }
    }

    if (showGoalDialog) {
        GoalDialog(
            initialGoal = uiState.goal,
            onDismiss = { showGoalDialog = false },
            onConfirm = { goal ->
                onGoalChange(goal)
                showGoalDialog = false
            },
        )
    }
}

@Composable
private fun GoalProgressCard(
    uiState: GoalsUiState,
    onEditGoal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.goals_year_title, uiState.year),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.goals_progress, uiState.readThisYear, uiState.goal),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            val remaining = uiState.goal - uiState.readThisYear
            Text(
                text = if (remaining <= 0) {
                    stringResource(R.string.goals_done)
                } else {
                    stringResource(R.string.goals_remaining, remaining)
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onEditGoal) {
                Text(stringResource(R.string.goals_edit))
            }
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GoalDialog(
    initialGoal: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var text by rememberSaveable { mutableStateOf(initialGoal.toString()) }
    val value = text.toIntOrNull()
    val isValid = value != null && value in GoalsViewModel.GOAL_RANGE

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.goals_dialog_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { input -> text = input.filter { it.isDigit() }.take(3) },
                label = { Text(stringResource(R.string.goals_dialog_label)) },
                isError = !isValid,
                supportingText = if (!isValid) {
                    { Text(stringResource(R.string.goals_dialog_error)) }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (value != null && isValid) onConfirm(value) },
                enabled = isValid,
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
