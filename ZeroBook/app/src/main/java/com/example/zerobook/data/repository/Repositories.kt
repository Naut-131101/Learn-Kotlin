package com.example.zerobook.data.repository

import com.example.zerobook.model.AppUser
import com.example.zerobook.model.Book
import com.example.zerobook.model.ReadingStatus
import com.example.zerobook.model.ShelfBook
import com.example.zerobook.model.ThemeMode
import com.example.zerobook.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/*
 * ViewModel chỉ phụ thuộc vào các interface này, không phụ thuộc Room/Retrofit/Firebase.
 * Nhờ vậy unit test có thể thay bằng bản giả (fake) chạy hoàn toàn trên JVM.
 */

interface BookRepository {
    suspend fun search(query: String): Result<List<Book>>
    suspend fun getBook(bookId: String): Result<Book>
}

interface ShelfRepository {
    fun observeShelf(): Flow<List<ShelfBook>>
    fun observeShelfBook(bookId: String): Flow<ShelfBook?>
    suspend fun addOrUpdateStatus(book: Book, status: ReadingStatus)
    suspend fun updateReview(bookId: String, rating: Int, note: String)
    suspend fun remove(bookId: String)

    /** Xếp lịch đồng bộ nền bằng WorkManager (chạy khi có mạng). */
    fun requestSync()

    /** Đồng bộ ngay: đẩy thay đổi lên Firestore rồi kéo dữ liệu mới về. Trả về true nếu thành công. */
    suspend fun syncNow(): Boolean

    /** Xoá toàn bộ dữ liệu trong máy (dùng khi đăng xuất). */
    suspend fun clearLocal()
}

interface AuthRepository {
    /** Phát ra user hiện tại, hoặc null khi chưa đăng nhập. */
    val currentUser: Flow<AppUser?>
    suspend fun signInWithGoogleIdToken(idToken: String): Result<AppUser>
    suspend fun signOut()
}

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setYearlyGoal(goal: Int)
    suspend fun setReminder(enabled: Boolean, hour: Int, minute: Int)
}
