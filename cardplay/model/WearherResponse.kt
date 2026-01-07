package com.example.cardplay.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("reason") val reason: String,
    @SerializedName("result") val result: WeatherResult,
    @SerializedName("error_code") val errorCode: Int
)

data class WeatherResult(
    @SerializedName("city") val city: String,
    @SerializedName("realtime") val realtime: RealtimeWeather,
    @SerializedName("future") val future: List<FutureWeather>
)

data class RealtimeWeather(
    @SerializedName("temperature") val temperature: String,
    @SerializedName("humidity") val humidity: String,
    @SerializedName("info") val info: String,
    @SerializedName("wid") val wid: String,
    @SerializedName("direct") val direct: String,
    @SerializedName("power") val power: String,
    @SerializedName("aqi") val aqi: String
)

data class FutureWeather(
    @SerializedName("date") val date: String,
    @SerializedName("temperature") val temperature: String,
    @SerializedName("weather") val weather: String,
    @SerializedName("wid") val wid: Wid,
    @SerializedName("direct") val direct: String
)

data class Wid(
    @SerializedName("day") val day: String,
    @SerializedName("night") val night: String
)