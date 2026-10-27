package com.example.bepviet.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class RecipeRepository {

    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())

    /** Id các món yêu thích. Chỉ repository được sửa; bên ngoài chỉ đọc. */
    val favoriteIds: StateFlow<Set<Int>> = _favoriteIds.asStateFlow()

    /** Danh sách món. Cold flow: mỗi lần collect "tải" lại, mất 800 ms như gọi mạng. */
    fun getRecipes(): Flow<List<Recipe>> = flow {
        delay(800)
        emit(RecipeData.recipes)
    }

    /** Một món theo id, null nếu không tồn tại. */
    fun getRecipe(id: Int): Flow<Recipe?> =
        getRecipes().map { recipes -> recipes.find { it.id == id } }

    fun toggleFavorite(id: Int) {
        _favoriteIds.update { ids -> if (id in ids) ids - id else ids + id }
    }
}