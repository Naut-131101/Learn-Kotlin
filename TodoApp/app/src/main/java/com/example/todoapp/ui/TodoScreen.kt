package com.example.todoapp.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.todoapp.R
import com.example.todoapp.model.Task
import com.example.todoapp.model.TaskFilter
import com.example.todoapp.ui.components.AddTaskBar
import com.example.todoapp.ui.components.EmptyState
import com.example.todoapp.ui.components.FilterBar
import com.example.todoapp.ui.components.TaskRow
import com.example.todoapp.ui.theme.TodoAppTheme

// Tầng stateful: nối state holder với UI
@Composable
fun TodoScreen(
    modifier: Modifier = Modifier,
    holder: TodoStateHolder = rememberTodoStateHolder(),
) {
    TodoContent(
        tasks = holder.visibleTasks,
        filter = holder.filter,
        newTaskTitle = holder.newTaskTitle,
        remainingCount = holder.remainingCount,
        hasCompleted = holder.hasCompleted,
        onNewTaskTitleChange = holder::onNewTaskTitleChange,
        onAddTask = holder::addTask,
        onToggleTask = holder::toggleTask,
        onDeleteTask = holder::deleteTask,
        onFilterChange = holder::changeFilter,
        onClearCompleted = holder::clearCompleted,
        modifier = modifier
    )
}

// Tầng stateless: chỉ hiển thị dữ liệu và báo sự kiện lên
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoContent(
    tasks: List<Task>,
    filter: TaskFilter,
    newTaskTitle: String,
    remainingCount: Int,
    hasCompleted: Boolean,
    onNewTaskTitleChange: (String) -> Unit,
    onAddTask: () -> Unit,
    onToggleTask: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onFilterChange: (TaskFilter) -> Unit,
    onClearCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AddTaskBar(
                title = newTaskTitle,
                onTitleChange = onNewTaskTitleChange,
                onAdd = onAddTask,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            FilterBar(
                selected = filter,
                onFilterChange = onFilterChange,
                remainingCount = remainingCount,
                canClearCompleted = hasCompleted,
                onClearCompleted = onClearCompleted,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            HorizontalDivider()

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(filter = filter)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            onToggle = { onToggleTask(task.id) },
                            onDelete = { onDeleteTask(task.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

private val previewTasks = listOf(
    Task(id = 3, title = "Nộp lab tuần 5", createdAt = 1_760_000_000_000),
    Task(id = 2, title = "Ôn bài state hoisting", isDone = true, createdAt = 1_759_990_000_000),
    Task(id = 1, title = "Mua sữa", createdAt = 1_759_980_000_000),
)

@Preview(name = "Có dữ liệu", showBackground = true, showSystemUi = true)
@Composable
private fun TodoContentPreview() {
    TodoAppTheme {
        TodoContent(
            tasks = previewTasks,
            filter = TaskFilter.ALL,
            newTaskTitle = "",
            remainingCount = 2,
            hasCompleted = true,
            onNewTaskTitleChange = {}, onAddTask = {}, onToggleTask = {},
            onDeleteTask = {}, onFilterChange = {}, onClearCompleted = {}
        )
    }
}

@Preview(
    name = "Rỗng - tối",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun TodoContentEmptyPreview() {
    TodoAppTheme {
        TodoContent(
            tasks = emptyList(),
            filter = TaskFilter.ALL,
            newTaskTitle = "Việc đang gõ",
            remainingCount = 0,
            hasCompleted = false,
            onNewTaskTitleChange = {}, onAddTask = {}, onToggleTask = {},
            onDeleteTask = {}, onFilterChange = {}, onClearCompleted = {}
        )
    }
}