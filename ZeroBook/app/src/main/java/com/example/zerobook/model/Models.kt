package com.example.zerobook.model

/** Trạng thái của một cuốn sách trên kệ. Thứ tự khai báo = thứ tự hiển thị trên tab. */
enum class ReadingStatus { READING, WANT_TO_READ, READ }

/** Thông tin một cuốn sách (lấy từ Google Books hoặc từ kệ sách đã lưu). */
data class Book(
    val id: String,
    val title: String,
    val authors: List<String> = emptyList(),
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val pageCount: Int? = null,
    val publishedDate: String? = null,
    val publisher: String? = null,
)

/** Một cuốn sách người dùng đã đưa lên kệ, kèm trạng thái, đánh giá và ghi chú. */
data class ShelfBook(
    val book: Book,
    val status: ReadingStatus,
    val rating: Int = 0,
    val note: String = "",
    val addedAt: Long = 0L,
    val finishedAt: Long? = null,
    val updatedAt: Long = 0L,
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val yearlyGoal: Int = DEFAULT_YEARLY_GOAL,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
) {
    companion object {
        const val DEFAULT_YEARLY_GOAL = 12
    }
}

data class AppUser(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
)
