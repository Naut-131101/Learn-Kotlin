package com.example.zerobook.fakes

import com.example.zerobook.data.repository.AuthRepository
import com.example.zerobook.data.repository.BookRepository
import com.example.zerobook.data.repository.ShelfRepository
import com.example.zerobook.data.repository.UserPreferencesRepository
import com.example.zerobook.model.AppUser
import com.example.zerobook.model.Book
import com.example.zerobook.model.ReadingStatus
import com.example.zerobook.model.ShelfBook
import com.example.zerobook.model.ThemeMode
import com.example.zerobook.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/*
 * Bản giả của các repository: lưu dữ liệu trong bộ nhớ, không cần Room/Firebase/mạng.
 * Test điều khiển dữ liệu trực tiếp qua các MutableStateFlow.
 */

fun testBook(id: String, title: String = "Book $id", pageCount: Int? = 200) = Book(
    id = id,
    title = title,
    authors = listOf("Author $id"),
    pageCount = pageCount,
)

fun testShelfBook(
    id: String,
    status: ReadingStatus,
    finishedAt: Long? = null,
    pageCount: Int? = 200,
) = ShelfBook(
    book = testBook(id, pageCount = pageCount),
    status = status,
    finishedAt = finishedAt,
)

class FakeShelfRepository : ShelfRepository {
    val books = MutableStateFlow<List<ShelfBook>>(emptyList())
    var syncRequestCount = 0
        private set

    override fun observeShelf(): Flow<List<ShelfBook>> = books

    override fun observeShelfBook(bookId: String): Flow<ShelfBook?> =
        books.map { list -> list.firstOrNull { it.book.id == bookId } }

    override suspend fun addOrUpdateStatus(book: Book, status: ReadingStatus) {
        books.update { list -> list.filterNot { it.book.id == book.id } + ShelfBook(book, status) }
    }

    override suspend fun updateReview(bookId: String, rating: Int, note: String) {
        books.update { list ->
            list.map { if (it.book.id == bookId) it.copy(rating = rating, note = note) else it }
        }
    }

    override suspend fun remove(bookId: String) {
        books.update { list -> list.filterNot { it.book.id == bookId } }
    }

    override fun requestSync() {
        syncRequestCount++
    }

    override suspend fun syncNow(): Boolean = true

    override suspend fun clearLocal() {
        books.value = emptyList()
    }
}

class FakeBookRepository : BookRepository {
    var searchResult: Result<List<Book>> = Result.success(emptyList())
    var lastQuery: String? = null
        private set

    override suspend fun search(query: String): Result<List<Book>> {
        lastQuery = query
        return searchResult
    }

    override suspend fun getBook(bookId: String): Result<Book> = Result.success(testBook(bookId))
}

class FakeUserPreferencesRepository : UserPreferencesRepository {
    val prefs = MutableStateFlow(UserPreferences())

    override val preferences: Flow<UserPreferences> = prefs

    override suspend fun setThemeMode(mode: ThemeMode) {
        prefs.update { it.copy(themeMode = mode) }
    }

    override suspend fun setYearlyGoal(goal: Int) {
        prefs.update { it.copy(yearlyGoal = goal) }
    }

    override suspend fun setReminder(enabled: Boolean, hour: Int, minute: Int) {
        prefs.update { it.copy(reminderEnabled = enabled, reminderHour = hour, reminderMinute = minute) }
    }
}

class FakeAuthRepository : AuthRepository {
    val user = MutableStateFlow<AppUser?>(null)
    var signInResult: Result<AppUser> = Result.success(
        AppUser(uid = "uid-1", displayName = "Test User", email = "test@example.com", photoUrl = null),
    )
    var receivedIdToken: String? = null
        private set

    override val currentUser: Flow<AppUser?> = user

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<AppUser> {
        receivedIdToken = idToken
        signInResult.onSuccess { user.value = it }
        return signInResult
    }

    override suspend fun signOut() {
        user.value = null
    }
}
