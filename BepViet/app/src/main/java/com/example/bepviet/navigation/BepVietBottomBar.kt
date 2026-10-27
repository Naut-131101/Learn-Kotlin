package com.example.bepviet.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy

data class TopLevelRoute<T : Any>(val label: String, val route: T, val icon: ImageVector)

val topLevelRoutes = listOf(
    TopLevelRoute("Công thức", RecipeList, Icons.Filled.Home),
    TopLevelRoute("Giới thiệu", About, Icons.Filled.Info),
)

@Composable
fun BepVietBottomBar(
    currentDestination: NavDestination?,
    onNavigate: (Any) -> Unit,
) {
    NavigationBar {
        topLevelRoutes.forEach { item ->
            val selected = currentDestination?.hierarchy
                ?.any { it.hasRoute(item.route::class) } == true
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}