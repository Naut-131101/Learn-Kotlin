package com.example.bepviet.navigation

import kotlinx.serialization.Serializable

@Serializable
object RecipeList

@Serializable
data class RecipeDetail(val recipeId: Int)

@Serializable
object Favorites

@Serializable
object About