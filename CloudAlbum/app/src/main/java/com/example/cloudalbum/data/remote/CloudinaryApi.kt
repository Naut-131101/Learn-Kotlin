package com.example.cloudalbum.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

/** Upload thẳng lên Cloudinary. URL đầy đủ do server trả về nên dùng @Url thay cho base URL. */
interface CloudinaryApi {

    @Multipart
    @POST
    suspend fun upload(
        @Url uploadUrl: String,
        @Part file: MultipartBody.Part,
        @Part("api_key") apiKey: RequestBody,
        @Part("timestamp") timestamp: RequestBody,
        @Part("folder") folder: RequestBody,
        @Part("tags") tags: RequestBody,
        @Part("signature") signature: RequestBody,
    ): CloudinaryUploadResult
}