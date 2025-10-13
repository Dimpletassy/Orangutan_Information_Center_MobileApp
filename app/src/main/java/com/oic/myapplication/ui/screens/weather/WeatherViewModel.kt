package com.oic.myapplication.ui.screens.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oic.myapplication.data.weather.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class WeatherUiState(
    val loading: Boolean = false,
    val temperatureC: Int? = null,
    val humidityPct: Int? = null,
    val windKmh: Int? = null,
    val error: String? = null
)

class WeatherViewModel(
    private val repo: WeatherRepository = WeatherRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(WeatherUiState())
    val state: StateFlow<WeatherUiState> = _state

    fun refresh(lat: Double, lon: Double) {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val result = repo.fetchCurrent(lat, lon)
            _state.value = result.fold(
                onSuccess = { cur: Current ->
                    WeatherUiState(
                        loading = false,
                        temperatureC = cur.temperatureC?.toInt(),
                        humidityPct = cur.humidityPct?.toInt(),
                        windKmh = cur.windKmh?.toInt()
                    )
                },
                onFailure = { e ->
                    _state.value.copy(loading = false, error = e.message ?: "Weather error")
                }
            )
        }
    }
}
