package com.example.zerobook.ui.navigation

import kotlinx.serialization.Serializable

/*
 * Route type-safe: mỗi màn hình là một class. Truyền thiếu tham số → compiler báo lỗi ngay,
 * thay vì crash lúc chạy như cách dùng chuỗi "detail/{bookId}".
 * Màn Đăng nhập không nằm trong NavHost: app tự chuyển dựa trên trạng thái đăng nhập.
 */

@Serializable data object ShelfRoute
@Serializable data object SearchRoute
@Serializable data object GoalsRoute
@Serializable data object SettingsRoute

@Serializable data class BookDetailRoute(val bookId: String)
@Serializable data class BookNotesRoute(val bookId: String)
