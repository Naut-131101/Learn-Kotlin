package com.example.todoapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.todoapp.R
import com.example.todoapp.model.TaskFilter

@Composable
fun FilterBar(
    selected: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit,
    remainingCount: Int,
    canClearCompleted: Boolean,
    onClearCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskFilter.entries.forEach { filter ->
                FilterChip(
                    selected = filter == selected,
                    onClick = { onFilterChange(filter) },
                    label = { Text(stringResource(filter.labelRes)) }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.remaining_count, remainingCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onClearCompleted, enabled = canClearCompleted) {
                Text(stringResource(R.string.clear_completed))
            }
        }
    }
}