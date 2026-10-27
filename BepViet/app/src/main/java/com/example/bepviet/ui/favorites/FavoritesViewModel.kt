package com.example.bepviet.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bepviet.BepVietApplication
import com.example.bepviet.data.Recipe
import com.example.bepviet.data.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data class Success(val recipes: List<Recipe>) : FavoritesUiState
}

class FavoritesViewModel(private val repository: RecipeRepository) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = combine(
        repository.getRecipes(),
        repository.favoriteIds,
    ) { recipes, favoriteIds ->
        FavoritesUiState.Success(recipes.filter { it.id in favoriteIds })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FavoritesUiState.Loading,
    )

    fun onToggleFavorite(recipeId: Int) = repository.toggleFavorite(recipeId)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as BepVietApplication
                FavoritesViewModel(app.container.recipeRepository)
            }
        }
    }
}