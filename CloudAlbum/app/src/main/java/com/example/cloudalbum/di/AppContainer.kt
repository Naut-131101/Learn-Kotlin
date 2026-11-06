package com.example.cloudalbum.di

import android.content.Context
import com.example.cloudalbum.BuildConfig
import com.example.cloudalbum.data.PhotoRepository
import com.example.cloudalbum.data.remote.AlbumApi
import com.example.cloudalbum.data.remote.CloudinaryApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                // BASIC: chỉ in method, URL, mã trả về — BODY sẽ in cả MB dữ liệu ảnh ra Logcat
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                else HttpLoggingInterceptor.Level.NONE
            }
        )
        .connectTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)   // upload ảnh qua mạng chậm cần thời gian
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.SERVER_URL)       // server Node.js
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val photoRepository = PhotoRepository(
        context = context,
        albumApi = retrofit.create(AlbumApi::class.java),
        cloudinaryApi = retrofit.create(CloudinaryApi::class.java), // dùng @Url nên base URL không quan trọng
    )
}