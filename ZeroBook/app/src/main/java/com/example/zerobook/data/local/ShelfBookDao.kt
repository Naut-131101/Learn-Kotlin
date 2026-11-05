package com.example.zerobook.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ShelfBookDao {

    /** Flow tự phát dữ liệu mới mỗi khi bảng thay đổi → UI luôn cập nhật. */
    @Query("SELECT * FROM shelf_books WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ShelfBookEntity>>

    @Query("SELECT * FROM shelf_books WHERE bookId = :bookId AND isDeleted = 0")
    fun observeById(bookId: String): Flow<ShelfBookEntity?>

    @Query("SELECT * FROM shelf_books WHERE bookId = :bookId")
    suspend fun getById(bookId: String): ShelfBookEntity?

    @Query("SELECT * FROM shelf_books")
    suspend fun getAll(): List<ShelfBookEntity>

    @Query("SELECT * FROM shelf_books WHERE pendingSync = 1")
    suspend fun getPending(): List<ShelfBookEntity>

    /** Insert nếu chưa có, update nếu đã có (theo khoá chính bookId). */
    @Upsert
    suspend fun upsert(entity: ShelfBookEntity)

    @Query("DELETE FROM shelf_books WHERE bookId = :bookId")
    suspend fun deleteById(bookId: String)

    @Query("DELETE FROM shelf_books")
    suspend fun clear()
}
