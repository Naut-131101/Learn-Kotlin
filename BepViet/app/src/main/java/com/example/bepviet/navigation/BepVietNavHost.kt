package com.example.bepviet.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.bepviet.ui.about.AboutScreen
import com.example.bepviet.ui.detail.RecipeDetailScreen
import com.example.bepviet.ui.list.RecipeListScreen

@Composable
fun BepVietNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val bottomBar: @Composable () -> Unit = {
        BepVietBottomBar(
            currentDestination = currentDestination,
            onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
        )
    }

    NavHost(navController = navController, startDestination = RecipeList) {
        composable<RecipeList> {
            RecipeListScreen(
                onRecipeClick = { id -> navController.navigate(RecipeDetail(recipeId = id)) },
                bottomBar = bottomBar,
            )
        }
        composable<RecipeDetail> { entry ->
            val route = entry.toRoute<RecipeDetail>()
            RecipeDetailScreen(
                recipeId = route.recipeId,
                onBack = { navController.navigateUp() },
            )
        }
        composable<About> {
            AboutScreen(bottomBar = bottomBar)
        }
    }
}