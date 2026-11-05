package com.example.zerobook.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Google Books API — đọc công khai, không cần API key. */
interface GoogleBooksApi {

    @GET("volumes")
    suspend fun search(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 20,
        @Query("printType") printType: String = "books",
    ): VolumesResponseDto

    @GET("volumes/{id}")
    suspend fun getVolume(@Path("id") id: String): VolumeDto

    companion object {
        const val BASE_URL = "https://www.googleapis.com/books/v1/"
    }
}
