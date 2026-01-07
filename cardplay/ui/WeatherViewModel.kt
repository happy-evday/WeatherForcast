package com.example.cardplay.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cardplay.model.WeatherResponse
import com.example.cardplay.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {
    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData: StateFlow<WeatherResponse?> = _weatherData.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchWeather(city: String = "北京") {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                val response = RetrofitClient.weatherApiService.getWeather(city)
                if (response.errorCode == 0) {
                    _weatherData.value = response
                } else {
                    _error.value = response.reason
                }
            } catch (e: Exception) {
                _error.value = "网络请求失败: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}