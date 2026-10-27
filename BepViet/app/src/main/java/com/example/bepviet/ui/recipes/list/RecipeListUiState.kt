package com.example.bepviet.ui.recipes.list

import com.example.bepviet.data.Recipe

sealed interface RecipeListUiState {
    data object Loading : RecipeListUiState

    data class Success(
        val recipes: List<Recipe>,
        val favoriteIds: Set<Int>,
        val categories: List<String>,
        val selectedCategory: String?,
    ) : RecipeListUiState
}