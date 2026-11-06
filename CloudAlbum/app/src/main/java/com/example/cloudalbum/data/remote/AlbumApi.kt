package com.example.cloudalbum.data.remote

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/** Ba endpoint của server Node.js. */
interface AlbumApi {

    @POST("api/uploads/signature")
    suspend fun getUploadSignature(): UploadSignature

    @GET("api/photos")
    suspend fun getPhotos(): List<Photo>

    @DELETE("api/photos")
    suspend fun deletePhoto(@Query("id") id: String): DeleteResult
}