package com.example.bepviet.di

import com.example.bepviet.data.RecipeRepository

/** Nơi tạo và giữ các dependency dùng chung cho toàn app. Tuần 9 Hilt sẽ thay thế. */
interface AppContainer {
    val recipeRepository: RecipeRepository
}

class DefaultAppContainer : AppContainer {
    override val recipeRepository: RecipeRepository by lazy { RecipeRepository() }
}