package com.example.zerobook.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.zerobook.model.Book
import com.example.zerobook.model.ReadingStatus
import com.example.zerobook.model.ShelfBook

/**
 * Một dòng trong bảng kệ sách. Room là nguồn dữ liệu chính (single source of truth):
 * UI chỉ đọc từ bảng này, Firestore chỉ là bản sao lưu để đồng bộ giữa các máy.
 *
 * - [pendingSync] = true: thay đổi chưa được đẩy lên Firestore.
 * - [isDeleted] = true: người dùng đã xoá, chờ xoá trên Firestore rồi mới xoá hẳn ở máy.
 */
@Entity(tableName = "shelf_books")
data class ShelfBookEntity(
    @PrimaryKey val bookId: String,
    val title: String,
    val authors: String,
    val description: String?,
    val thumbnailUrl: String?,
    val pageCount: Int?,
    val publishedDate: String?,
    val publisher: String?,
    val status: ReadingStatus,
    val rating: Int,
    val note: String,
    val addedAt: Long,
    val finishedAt: Long?,
    val updatedAt: Long,
    val pendingSync: Boolean,
    val isDeleted: Boolean,
)

/** Danh sách tác giả được lưu thành một chuỗi, ngăn cách bằng ký tự xuống dòng. */
private const val LIST_SEPARATOR = "\n"

fun ShelfBookEntity.toDomain(): ShelfBook = ShelfBook(
    book = Book(
        id = bookId,
        title = title,
        authors = authors.split(LIST_SEPARATOR).filter { it.isNotBlank() },
        description = description,
        thumbnailUrl = thumbnailUrl,
        pageCount = pageCount,
        publishedDate = publishedDate,
        publisher = publisher,
    ),
    status = status,
    rating = rating,
    note = note,
    addedAt = addedAt,
    finishedAt = finishedAt,
    updatedAt = updatedAt,
)

fun Book.toShelfEntity(
    status: ReadingStatus,
    rating: Int,
    note: String,
    addedAt: Long,
    finishedAt: Long?,
    updatedAt: Long,
): ShelfBookEntity = ShelfBookEntity(
    bookId = id,
    title = title,
    authors = authors.joinToString(LIST_SEPARATOR),
    description = description,
    thumbnailUrl = thumbnailUrl,
    pageCount = pageCount,
    publishedDate = publishedDate,
    publisher = publisher,
    status = status,
    rating = rating,
    note = note,
    addedAt = addedAt,
    finishedAt = finishedAt,
    updatedAt = updatedAt,
    pendingSync = true,
    isDeleted = false,
)
