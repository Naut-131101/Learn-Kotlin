package com.example.zerobook.data.repository

import android.util.Log
import com.example.zerobook.data.remote.GoogleBooksApi
import com.example.zerobook.data.remote.toDomain
import com.example.zerobook.data.util.suspendRunCatching
import com.example.zerobook.model.Book
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor(
    private val api: GoogleBooksApi,
) : BookRepository {

    override suspend fun search(query: String): Result<List<Book>> = suspendRunCatching {
        // API đôi khi trả trùng id → distinctBy để LazyColumn không bị lỗi trùng key
        api.search(query).items.map { it.toDomain() }.distinctBy { it.id }
    }.onFailure { Log.e(TAG, "search failed", it) }

    override suspend fun getBook(bookId: String): Result<Book> = suspendRunCatching {
        api.getVolume(bookId).toDomain()
    }.onFailure { Log.e(TAG, "getBook failed", it) }

    private companion object {
        const val TAG = "BookRepository"
    }
}