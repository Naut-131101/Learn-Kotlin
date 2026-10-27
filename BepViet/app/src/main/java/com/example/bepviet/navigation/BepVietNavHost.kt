package com.example.bepviet.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bepviet.ui.about.AboutScreen
import com.example.bepviet.ui.favorites.FavoritesRoute
import com.example.bepviet.ui.recipes.detail.RecipeDetailRoute
import com.example.bepviet.ui.recipes.list.RecipeListRoute

@Composable
fun BepVietNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    val bottomBar: @Composable () -> Unit = {
        BepVietBottomBar(
            currentDestination = backStackEntry?.destination,
            onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
        )
    }
    val openDetail: (Int) -> Unit = { id -> navController.navigate(RecipeDetail(recipeId = id)) }

    NavHost(navController = navController, startDestination = RecipeList) {
        composable<RecipeList> {
            RecipeListRoute(onRecipeClick = openDetail, bottomBar = bottomBar)
        }
        composable<RecipeDetail> {
            // Không cần toRoute ở đây: RecipeDetailViewModel tự đọc từ SavedStateHandle
            RecipeDetailRoute(onBack = { navController.navigateUp() })
        }
        composable<Favorites> {
            FavoritesRoute(onRecipeClick = openDetail, bottomBar = bottomBar)
        }
        composable<About> {
            AboutScreen(bottomBar = bottomBar)
        }
    }
}