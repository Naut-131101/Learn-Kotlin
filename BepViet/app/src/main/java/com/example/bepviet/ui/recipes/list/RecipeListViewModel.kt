package com.example.bepviet.ui.recipes.list

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bepviet.BepVietApplication
import com.example.bepviet.data.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class RecipeListViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: RecipeRepository,
) : ViewModel() {

    init {
        Log.d("BepViet", "RecipeListViewModel created")
    }

    /** Từ khoá tìm kiếm: lưu trong SavedStateHandle nên sống qua cả process death. */
    val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    private val selectedCategory: StateFlow<String?> =
        savedStateHandle.getStateFlow<String?>(KEY_CATEGORY, null)

    val uiState: StateFlow<RecipeListUiState> = combine(
        repository.getRecipes(),
        repository.favoriteIds,
        query,
        selectedCategory,
    ) { recipes, favoriteIds, query, category ->
        val keyword = query.trim()
        val filtered = recipes.filter { recipe ->
            (category == null || recipe.category == category) &&
                    (keyword.isEmpty() || recipe.name.contains(keyword, ignoreCase = true))
        }
        RecipeListUiState.Success(
            recipes = filtered,
            favoriteIds = favoriteIds,
            categories = recipes.map { it.category }.distinct(),
            selectedCategory = category,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecipeListUiState.Loading,
    )

    fun onQueryChange(newQuery: String) {
        savedStateHandle[KEY_QUERY] = newQuery
    }

    fun onCategorySelect(category: String?) {
        savedStateHandle[KEY_CATEGORY] = category
    }

    fun onToggleFavorite(recipeId: Int) {
        repository.toggleFavorite(recipeId)
    }

    override fun onCleared() {
        Log.d("BepViet", "RecipeListViewModel cleared")
    }

    companion object {
        private const val KEY_QUERY = "query"
        private const val KEY_CATEGORY = "category"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as BepVietApplication
                RecipeListViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = app.container.recipeRepository,
                )
            }
        }
    }
}