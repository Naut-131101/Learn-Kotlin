package com.example.zerobook.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.zerobook.ui.detail.BookDetailScreen
import com.example.zerobook.ui.goals.GoalsScreen
import com.example.zerobook.ui.notes.BookNotesScreen
import com.example.zerobook.ui.search.SearchScreen
import com.example.zerobook.ui.settings.SettingsScreen
import com.example.zerobook.ui.shelf.ShelfScreen

@Composable
fun ZeroBookNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val openBook: (String) -> Unit = { bookId -> navController.navigate(BookDetailRoute(bookId)) }

    NavHost(
        navController = navController,
        startDestination = ShelfRoute,
        modifier = modifier,
        // Animation chuyển màn: màn mới trượt nhẹ từ phải vào và hiện dần,
        // bấm Back thì màn hiện tại trượt nhẹ sang phải và mờ đi.
        enterTransition = {
            fadeIn(tween(TRANSITION_MS)) +
                slideInHorizontally(tween(TRANSITION_MS)) { fullWidth -> fullWidth / 10 }
        },
        exitTransition = { fadeOut(tween(TRANSITION_MS / 2)) },
        popEnterTransition = { fadeIn(tween(TRANSITION_MS)) },
        popExitTransition = {
            fadeOut(tween(TRANSITION_MS / 2)) +
                slideOutHorizontally(tween(TRANSITION_MS)) { fullWidth -> fullWidth / 10 }
        },
    ) {
        composable<ShelfRoute> {
            ShelfScreen(
                onBookClick = openBook,
                onFindBooksClick = { navController.navigateToTopLevel(SearchRoute) },
            )
        }

        composable<SearchRoute> {
            SearchScreen(onBookClick = openBook)
        }

        composable<GoalsRoute> {
            GoalsScreen(onBookClick = openBook)
        }

        composable<SettingsRoute> {
            SettingsScreen()
        }

        composable<BookDetailRoute> {
            // bookId được BookDetailViewModel đọc từ SavedStateHandle
            BookDetailScreen(
                // dropUnlessResumed: bấm Back 2 lần thật nhanh cũng chỉ quay lại 1 màn
                onBack = dropUnlessResumed { navController.popBackStack() },
                onOpenNotes = { bookId -> navController.navigate(BookNotesRoute(bookId)) },
            )
        }

        composable<BookNotesRoute> {
            BookNotesScreen(
                onBack = dropUnlessResumed { navController.popBackStack() },
            )
        }
    }
}

private const val TRANSITION_MS = 280

/**
 * Chuyển tab:
 * - popUpTo(start) + saveState: không chồng màn khi đổi tab, nhưng lưu trạng thái tab cũ
 * - launchSingleTop: bấm lại tab đang mở không tạo thêm bản sao
 * - restoreState: quay lại tab cũ thì khôi phục đúng chỗ đang xem
 */
fun NavHostController.navigateToTopLevel(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
