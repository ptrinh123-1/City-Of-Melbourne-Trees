package com.example.melbtrees.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface MelbourneTreesApi {
    // Updated to use the v2.1 endpoint and `where` parameter
    @GET("catalog/datasets/trees-with-species-and-dimensions-urban-forest/records")
    suspend fun getTrees(
        @Query("where") locationQuery: String, // The parameter is now "where"
        @Query("limit") limit: Int = 10 // The parameter is now "limit"
    ): ApiResponse
}