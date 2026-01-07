package com.example.cardplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cardplay.ui.WeatherViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                WeatherApp()
            }
        }
    }
}

@Composable
fun WeatherApp(
    viewModel: WeatherViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val weatherData by viewModel.weatherData.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    // 自动加载北京天气数据
    LaunchedEffect(Unit) {
        viewModel.fetchWeather("北京")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 错误提示
        error?.let { errorMessage ->
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("请求失败") },
                text = { Text(errorMessage) },
                confirmButton = {
                    Button(onClick = { viewModel.clearError() }) {
                        Text("确定")
                    }
                }
            )
        }

        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            weatherData?.let { data ->
                WeatherContent(data)
            }
        }
    }
}

@Composable
fun WeatherContent(weatherData: com.example.cardplay.model.WeatherResponse) {
    val realtime = weatherData.result.realtime
    val future = weatherData.result.future

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 第一部分：当前天气（占屏幕2/6）
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
                .clip(RoundedCornerShape(16.dp))
                .background(getWeatherColor(realtime.info))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 根据天气类型显示不同图标
                Image(
                    painter = painterResource(getWeatherIcon(realtime.info)),
                    contentDescription = "天气图标",
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${realtime.temperature}°C",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = realtime.info,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = weatherData.result.city,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                // 显示更多实时信息
                Text(
                    text = "湿度: ${realtime.humidity}% | 风力: ${realtime.power}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 第二部分：未来几天预报（左右滑动，占屏幕1/6）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = "未来几天预报",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(future.take(5)) { dayWeather ->
                    FutureWeatherCard(dayWeather)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 第三部分：详细天气预报（上下滑动，占屏幕3/6）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(3f)
        ) {
            Text(
                text = "详细天气预报",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(future) { dayWeather ->
                    DetailedWeatherCard(dayWeather)
                }
            }
        }
    }
}

@Composable
fun FutureWeatherCard(dayWeather: com.example.cardplay.model.FutureWeather) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(80.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 显示日期（简化的格式，如"10/25"）
            val dateParts = dayWeather.date.split("-")
            val simpleDate = if (dateParts.size >= 3) "${dateParts[1]}/${dateParts[2]}" else dayWeather.date
            Text(
                text = simpleDate,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            // 天气图标
            Image(
                painter = painterResource(getWeatherIcon(dayWeather.weather)),
                contentDescription = dayWeather.weather,
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dayWeather.temperature,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DetailedWeatherCard(dayWeather: com.example.cardplay.model.FutureWeather) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：日期和天气图标
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dayWeather.date,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Image(
                    painter = painterResource(getWeatherIcon(dayWeather.weather)),
                    contentDescription = dayWeather.weather,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 中间：详细信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = dayWeather.weather,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_grain),
                        contentDescription = "温度",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "温度: ${dayWeather.temperature}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_grain),
                        contentDescription = "风向",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "风向: ${dayWeather.direct}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // 右侧：更多信息
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "详细",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// 根据天气类型获取图标
fun getWeatherIcon(weatherInfo: String): Int {
    return when {
        weatherInfo.contains("晴") -> R.drawable.qing  // 需要添加晴天图片
        weatherInfo.contains("多云") -> R.drawable.duoyun  // 需要添加多云图片
        weatherInfo.contains("阴") -> R.drawable.yin  // 需要添加阴天图片
        weatherInfo.contains("雨") -> R.drawable.yu  // 需要添加下雨图片
        weatherInfo.contains("雪") -> R.drawable.xue  // 使用现有的雪图片
        else -> R.drawable.xue  // 默认图片
    }
}

// 根据天气类型获取背景色
fun getWeatherColor(weatherInfo: String): Color {
    return when {
        weatherInfo.contains("晴") -> Color(0xFFFFD700)  // 金色
        weatherInfo.contains("多云") -> Color(0xFF87CEEB)  // 天蓝色
        weatherInfo.contains("阴") -> Color(0xFF778899)  // 石板灰
        weatherInfo.contains("雨") -> Color(0xFF4682B4)  // 钢蓝色
        weatherInfo.contains("雪") -> Color(0xFFF0F8FF)  // 爱丽丝蓝
        else -> Color(0xFF2196F3)  // 默认蓝色
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherAppPreview() {
    MaterialTheme {
        // 为了预览，创建一个模拟的WeatherResponse
        WeatherContent(
            com.example.cardplay.model.WeatherResponse(
                reason = "success",
                errorCode = 0,
                result = com.example.cardplay.model.WeatherResult(
                    city = "北京",
                    realtime = com.example.cardplay.model.RealtimeWeather(
                        temperature = "25",
                        humidity = "60",
                        info = "晴",
                        wid = "00",
                        direct = "北风",
                        power = "3级",
                        aqi = "45"
                    ),
                    future = listOf(
                        com.example.cardplay.model.FutureWeather(
                            date = "2024-10-25",
                            temperature = "18/25℃",
                            weather = "晴",
                            wid = com.example.cardplay.model.Wid("00", "00"),
                            direct = "北风"
                        ),
                        com.example.cardplay.model.FutureWeather(
                            date = "2024-10-26",
                            temperature = "17/24℃",
                            weather = "多云",
                            wid = com.example.cardplay.model.Wid("01", "01"),
                            direct = "东北风"
                        )
                    )
                )
            )
        )
    }
}
