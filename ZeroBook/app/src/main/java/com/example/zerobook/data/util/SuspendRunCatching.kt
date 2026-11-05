package com.example.zerobook.data.util

import kotlin.coroutines.cancellation.CancellationException

/**
 * Giống runCatching nhưng KHÔNG nuốt CancellationException.
 * Nếu nuốt, một coroutine đã bị huỷ (ví dụ lượt tìm kiếm cũ) vẫn chạy tiếp
 * và ghi đè kết quả của lượt tìm kiếm mới.
 */
suspend fun <T> suspendRunCatching(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
