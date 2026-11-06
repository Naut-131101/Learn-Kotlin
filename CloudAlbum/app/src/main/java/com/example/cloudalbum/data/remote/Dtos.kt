package com.example.cloudalbum.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Một ảnh trong album — đúng JSON mà server trả ở GET /api/photos. */
@Serializable
data class Photo(
    val id: String,          // public_id trên Cloudinary, ví dụ "android-album/abc123"
    val url: String,         // ảnh gốc
    val thumbUrl: String,    // ảnh vuông 400x400 do Cloudinary tự cắt
    val width: Int = 0,
    val height: Int = 0,
    val bytes: Long = 0,
    val createdAt: String = "",
)

/** Chữ ký server cấp cho MỘT lần upload (hết hạn sau 1 giờ). */
@Serializable
data class UploadSignature(
    val uploadUrl: String,
    val apiKey: String,
    val timestamp: Long,
    val folder: String,
    val tags: String,
    val signature: String,
)

@Serializable
data class DeleteResult(val deleted: Boolean)

/** Cloudinary trả rất nhiều trường sau khi upload; ta chỉ cần vài trường. */
@Serializable
data class CloudinaryUploadResult(
    @SerialName("public_id") val publicId: String,
    @SerialName("secure_url") val secureUrl: String,
)