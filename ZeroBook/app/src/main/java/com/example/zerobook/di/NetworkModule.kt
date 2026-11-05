package com.example.zerobook.di

import com.example.zerobook.BuildConfig
import com.example.zerobook.data.remote.GoogleBooksApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true // API trả rất nhiều trường ta không dùng
        coerceInputValues = true // gặp null ở trường không-null thì dùng giá trị mặc định
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            // Chỉ log request ở bản debug, bản release không log gì
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        // Tự thêm ?key=... vào mọi request tới Google Books.
        // Không có key thì Google Books dùng hạn mức chung và hay trả lỗi 429.
        val apiKeyInterceptor = Interceptor { chain ->
            val request = chain.request()
            val apiKey = BuildConfig.GOOGLE_BOOKS_API_KEY
            if (apiKey.isBlank() || request.url.host != "www.googleapis.com") {
                return@Interceptor chain.proceed(request)
            }
            val urlWithKey = request.url.newBuilder()
                .addQueryParameter("key", apiKey)
                .build()
            chain.proceed(request.newBuilder().url(urlWithKey).build())
        }

        return OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideGoogleBooksApi(client: OkHttpClient, json: Json): GoogleBooksApi =
        Retrofit.Builder()
            .baseUrl(GoogleBooksApi.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GoogleBooksApi::class.java)
}