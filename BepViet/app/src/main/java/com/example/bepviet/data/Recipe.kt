package com.example.bepviet.data

enum class Difficulty(val label: String) {
    EASY("Dễ"),
    MEDIUM("Trung bình"),
    HARD("Khó"),
}

data class Recipe(
    val id: Int,
    val name: String,
    val category: String,
    val emoji: String,
    val description: String,
    val cookTimeMin: Int,
    val difficulty: Difficulty,
    val ingredients: List<String>,
    val steps: List<String>,
)