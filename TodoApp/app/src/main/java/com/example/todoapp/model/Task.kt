package com.example.todoapp.model

import androidx.annotation.StringRes
import com.example.todoapp.R

data class Task(
    val id: Long,
    val title: String,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

enum class TaskFilter(@StringRes val labelRes: Int) {
    ALL(R.string.filter_all),
    ACTIVE(R.string.filter_active),
    DONE(R.string.filter_done),
}