package com.example.cardplay.network

import com.example.cardplay.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("simpleWeather/query")
    suspend fun getWeather(
        @Query("city") city: String,
        @Query("key") key: String = "46dc8f3bba0771e9581cef93cd882718"
    ): WeatherResponse
}