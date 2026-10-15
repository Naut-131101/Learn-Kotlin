package com.example.todoapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.todoapp.model.Task
import com.example.todoapp.model.TaskFilter

class TodoStateHolder(initialTasks: List<Task> = emptyList()) {

    // ---------- State (chỉ đọc từ bên ngoài) ----------
    var tasks by mutableStateOf(initialTasks)
        private set
    var filter by mutableStateOf(TaskFilter.ALL)
        private set
    var newTaskTitle by mutableStateOf("")
        private set

    private var nextId: Long = (initialTasks.maxOfOrNull { it.id } ?: 0L) + 1

    // ---------- Giá trị dẫn xuất (tính từ state, không lưu riêng) ----------
    val visibleTasks: List<Task>
        get() = when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.ACTIVE -> tasks.filter { !it.isDone }
            TaskFilter.DONE -> tasks.filter { it.isDone }
        }

    val remainingCount: Int
        get() = tasks.count { !it.isDone }

    val hasCompleted: Boolean
        get() = tasks.any { it.isDone }

    // ---------- Sự kiện (UI gọi lên) ----------
    fun onNewTaskTitleChange(text: String) {
        newTaskTitle = text
    }

    fun addTask() {
        val title = newTaskTitle.trim()
        if (title.isEmpty()) return                       // bỏ qua chuỗi rỗng
        val task = Task(id = nextId++, title = title)
        tasks = listOf(task) + tasks                      // danh sách MỚI, việc mới ở đầu
        newTaskTitle = ""
    }

    fun toggleTask(id: Long) {
        tasks = tasks.map { task ->
            if (task.id == id) task.copy(isDone = !task.isDone) else task
        }
    }

    fun deleteTask(id: Long) {
        tasks = tasks.filterNot { it.id == id }
    }

    fun changeFilter(newFilter: TaskFilter) {
        filter = newFilter
    }

    fun clearCompleted() {
        tasks = tasks.filterNot { it.isDone }
    }
}

@Composable
fun rememberTodoStateHolder(): TodoStateHolder = remember { TodoStateHolder() }