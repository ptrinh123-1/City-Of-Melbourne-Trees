package com.example.melbtrees.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface MelbourneTreesApi {
    @GET("catalog/datasets/trees-with-species-and-dimensions-urban-forest/records")
    suspend fun getTrees(@Query("limit") limit: Int = 100): ApiResponse
}