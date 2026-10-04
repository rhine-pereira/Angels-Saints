package com.rhinepereira.saints.data.remote

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Url

interface ContentApi {

    @GET("home.json")
    suspend fun getHome(
        @Header("Cache-Control") cacheControl: String? = "no-cache"
    ): CategoryResponse

    @GET("categories/{categoryId}.json")
    suspend fun getCategoryItems(
        @Path("categoryId") categoryId: String,
        @Header("Cache-Control") cacheControl: String? = null
    ): CategoryItemsResponse

    @GET("items/{contentId}.json")
    suspend fun getContent(
        @Path("contentId") contentId: String,
        @Header("Cache-Control") cacheControl: String? = null
    ): ContentResponse

    @GET
    suspend fun getDailyReadings(@Url url: String): DailyReadingsResponse
}
