package com.example.cloudalbum.data

import android.content.Context
import android.net.Uri
import com.example.cloudalbum.data.remote.AlbumApi
import com.example.cloudalbum.data.remote.CloudinaryApi
import com.example.cloudalbum.data.remote.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class PhotoRepository(
    private val context: Context,
    private val albumApi: AlbumApi,
    private val cloudinaryApi: CloudinaryApi,
) {
    suspend fun getPhotos(): List<Photo> = albumApi.getPhotos()

    /** Nén ảnh → xin chữ ký từ server → gửi thẳng lên Cloudinary. */
    suspend fun upload(uri: Uri) {
        // 1. Nén trên Dispatchers.Default: decode bitmap rất nặng, không được chạy trên main thread
        val image = withContext(Dispatchers.Default) { ImageCompressor.compress(context, uri) }

        // 2. Chữ ký chỉ dùng cho đúng các tham số này, nên phải gửi lên Cloudinary y nguyên
        val sig = albumApi.getUploadSignature()

        // 3. Upload multipart: file + các tham số đã ký
        val text = { value: String -> value.toRequestBody("text/plain".toMediaType()) }
        val file = MultipartBody.Part.createFormData(
            name = "file",
            filename = "photo.jpg",
            body = image.bytes.toRequestBody("image/jpeg".toMediaType()),
        )
        cloudinaryApi.upload(
            uploadUrl = sig.uploadUrl,
            file = file,
            apiKey = text(sig.apiKey),
            timestamp = text(sig.timestamp.toString()),
            folder = text(sig.folder),
            tags = text(sig.tags),
            signature = text(sig.signature),
        )
    }

    suspend fun delete(photo: Photo) {
        albumApi.deletePhoto(photo.id)
    }
}