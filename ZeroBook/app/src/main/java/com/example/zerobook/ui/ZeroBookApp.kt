package com.example.zerobook.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.zerobook.ui.login.LoginScreen
import com.example.zerobook.ui.navigation.TopLevelDestination
import com.example.zerobook.ui.navigation.ZeroBookNavHost
import com.example.zerobook.ui.navigation.navigateToTopLevel

/**
 * Chọn màn theo trạng thái đăng nhập: chưa đăng nhập → Login, đã đăng nhập → app chính.
 * Đăng xuất ở màn Cài đặt sẽ tự đưa về màn Login mà không cần điều hướng thủ công.
 */
@Composable
fun ZeroBookApp(authState: AuthState) {
    when (authState) {
        AuthState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        AuthState.SignedOut -> LoginScreen()
        is AuthState.SignedIn -> MainContent()
    }
}

/** Màn hình rộng từ 600dp (tablet, điện thoại xoay ngang) thì dùng thanh điều hướng bên trái. */
private val RailBreakpoint = 600.dp

@Composable
private fun MainContent() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // Chỉ hiện thanh điều hướng ở 4 màn chính, ẩn ở màn Chi tiết và Ghi chú
    val showNavigation = TopLevelDestination.entries.any { destination ->
        currentDestination?.hasRoute(destination.route::class) == true
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val useRail = maxWidth >= RailBreakpoint

        // Thanh điều hướng đã tự chừa chỗ cho thanh hệ thống,
        // nên báo cho các màn con biết phần inset đó đã được dùng (tránh bị cách 2 lần).
        val navHostModifier = when {
            !showNavigation -> Modifier
            useRail -> Modifier.consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
            else -> Modifier.consumeWindowInsets(WindowInsets.navigationBars)
        }

        // NavHost luôn nằm cùng một chỗ trong cây Compose, nên xoay màn hình
        // (đổi giữa bottom bar và rail) không làm mất màn đang mở.
        Row(Modifier.fillMaxSize()) {
            if (useRail) {
                AnimatedVisibility(
                    visible = showNavigation,
                    enter = expandHorizontally() + fadeIn(),
                    exit = shrinkHorizontally() + fadeOut(),
                ) {
                    NavigationRail(Modifier.fillMaxHeight()) {
                        TopLevelDestination.entries.forEach { destination ->
                            NavigationRailItem(
                                selected = currentDestination.isOn(destination),
                                onClick = { navController.navigateToTopLevel(destination.route) },
                                icon = { Icon(destination.icon, contentDescription = null) },
                                label = { Text(stringResource(destination.labelRes)) },
                            )
                        }
                    }
                }
            }

            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                ZeroBookNavHost(
                    navController = navController,
                    modifier = Modifier
                        .weight(1f)
                        .then(navHostModifier),
                )

                if (!useRail) {
                    AnimatedVisibility(
                        visible = showNavigation,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        NavigationBar {
                            TopLevelDestination.entries.forEach { destination ->
                                NavigationBarItem(
                                    selected = currentDestination.isOn(destination),
                                    onClick = { navController.navigateToTopLevel(destination.route) },
                                    // label đã được TalkBack đọc nên icon không cần mô tả
                                    icon = { Icon(destination.icon, contentDescription = null) },
                                    label = { Text(stringResource(destination.labelRes)) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun NavDestination?.isOn(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
