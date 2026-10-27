package com.example.bepviet.ui.recipes.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.example.bepviet.BepVietApplication
import com.example.bepviet.data.Recipe
import com.example.bepviet.data.RecipeRepository
import com.example.bepviet.navigation.RecipeDetail
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed interface RecipeDetailUiState {
    data object Loading : RecipeDetailUiState
    data object NotFound : RecipeDetailUiState
    data class Success(val recipe: Recipe, val isFavorite: Boolean) : RecipeDetailUiState
}

class RecipeDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: RecipeRepository,
) : ViewModel() {

    private val recipeId: Int = savedStateHandle.toRoute<RecipeDetail>().recipeId

    init {
        Log.d("BepViet", "RecipeDetailViewModel created for recipe $recipeId")
    }

    val uiState: StateFlow<RecipeDetailUiState> = combine(
        repository.getRecipe(recipeId),
        repository.favoriteIds,
    ) { recipe, favoriteIds ->
        if (recipe == null) {
            RecipeDetailUiState.NotFound
        } else {
            RecipeDetailUiState.Success(recipe = recipe, isFavorite = recipe.id in favoriteIds)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecipeDetailUiState.Loading,
    )

    fun toggleFavorite() {
        repository.toggleFavorite(recipeId)
    }

    override fun onCleared() {
        Log.d("BepViet", "RecipeDetailViewModel cleared for recipe $recipeId")
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as BepVietApplication
                RecipeDetailViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = app.container.recipeRepository,
                )
            }
        }
    }
}