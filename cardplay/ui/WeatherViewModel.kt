package com.example.cardplay.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardplay.model.WeatherResponse
import com.example.cardplay.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {
    // 当前选择的城市（状态）
    private val _currentCity = MutableStateFlow("北京")
    val currentCity: StateFlow<String> = _currentCity.asStateFlow()

    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData: StateFlow<WeatherResponse?> = _weatherData.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // 热门城市列表状态 - 新增
    private val _popularCities = MutableStateFlow(
        listOf("北京", "上海", "广州", "深圳", "成都", "杭州", "南京", "武汉", "西安", "重庆")
    )
    val popularCities: StateFlow<List<String>> = _popularCities.asStateFlow()

    // 核心方法：更新城市并获取天气
    fun updateCity(newCity: String) {
        if (newCity.isNotBlank() && newCity != _currentCity.value) {
            _currentCity.value = newCity
            fetchWeather(newCity)
        }
    }

    // 新增方法：添加城市到热门列表
    fun addCityToPopular(city: String) {
        val trimmedCity = city.trim()
        if (trimmedCity.isBlank()) return

        val currentList = _popularCities.value.toMutableList()

        // 检查是否已经存在（避免重复）
        if (!currentList.contains(trimmedCity)) {
            // 添加到列表最前面
            currentList.add(0, trimmedCity)
            _popularCities.value = currentList
        }
    }

    // 新增方法：从热门列表中删除一个城市（已修复）
    fun removeLastPopularCity() {
        val currentList = _popularCities.value.toMutableList()
        if (currentList.isNotEmpty()) {
            // ✅ 修复：使用兼容的方式删除最后一个元素
            currentList.removeAt(currentList.lastIndex)  // 或者 currentList.size - 1
            _popularCities.value = currentList
        }
    }

    // 原有的fetchWeather方法调整为private，通过updateCity调用
    private fun fetchWeather(city: String) {
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

    // 初始化时获取一次天气
    init {
        fetchWeather(_currentCity.value)
    }
}