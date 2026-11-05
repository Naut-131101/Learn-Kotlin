package com.example.zerobook.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.zerobook.R

/** Các tab trên thanh điều hướng dưới. Thêm/bớt tab chỉ cần sửa ở đây. */
enum class TopLevelDestination(
    val route: Any,
    val icon: ImageVector,
    @StringRes val labelRes: Int,
) {
    SHELF(ShelfRoute, Icons.AutoMirrored.Filled.MenuBook, R.string.nav_shelf),
    SEARCH(SearchRoute, Icons.Filled.Search, R.string.nav_search),
    GOALS(GoalsRoute, Icons.Filled.EmojiEvents, R.string.nav_goals),
    SETTINGS(SettingsRoute, Icons.Filled.Settings, R.string.nav_settings),
}
