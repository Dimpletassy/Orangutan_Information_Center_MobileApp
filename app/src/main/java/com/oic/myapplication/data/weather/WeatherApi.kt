package com.oic.myapplication.data.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    // Example:
    // https://api.open-meteo.com/v1/forecast?latitude=3.5952&longitude=98.6722&current=temperature_2m,relative_humidity_2m,wind_speed_10m&timezone=Asia%2FJakarta
    @GET("forecast")
    suspend fun getCurrent(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        // request current variables we need
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,wind_speed_10m",
        @Query("timezone") tz: String = "Asia/Jakarta"
    ): WeatherResponse
}

@Serializable
data class WeatherResponse(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timezone: String? = null,
    val current: Current? = null
)

@Serializable
data class Current(
    @SerialName("time") val timeIso: String? = null,
    @SerialName("temperature_2m") val temperatureC: Double? = null,
    @SerialName("relative_humidity_2m") val humidityPct: Double? = null,
    @SerialName("wind_speed_10m") val windKmh: Double? = null
)