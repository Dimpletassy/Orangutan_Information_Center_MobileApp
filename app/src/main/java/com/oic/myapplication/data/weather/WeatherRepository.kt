package com.oic.myapplication.data.weather

class WeatherRepository(
    private val api: WeatherApi = WeatherService.api
) {
    suspend fun fetchCurrent(lat: Double, lon: Double): Result<Current> = try {
        val res = api.getCurrent(lat, lon)
        val cur = res.current ?: error("No current weather")
        Result.success(cur)
    } catch (t: Throwable) {
        Result.failure(t)
    }
}