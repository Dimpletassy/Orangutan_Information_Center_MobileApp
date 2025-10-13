package com.oic.myapplication.data.weather

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

object WeatherService {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/v1/")
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val api: WeatherApi = retrofit.create(WeatherApi::class.java)
}