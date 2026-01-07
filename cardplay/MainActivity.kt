package com.example.cardplay

import android.os.Bundle
import kotlinx.coroutines.delay
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cardplay.model.*
import com.example.cardplay.ui.WeatherViewModel

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

// 定义导航路由
sealed class Screen(val route: String) {
    object WeatherHome : Screen("weather_home")
    object CitySelection : Screen("city_selection")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherApp(
    viewModel: WeatherViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.WeatherHome.route
    ) {
        // 天气主页
        composable(Screen.WeatherHome.route) {
            WeatherHomeScreen(viewModel, navController)
        }
        // 城市选择页
        composable(Screen.CitySelection.route) {
            CitySelectionScreen(viewModel, navController)
        }
    }
}

@Composable
fun WeatherHomeScreen(
    viewModel: WeatherViewModel,
    navController: NavHostController
) {
    val weatherData by viewModel.weatherData.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 顶部栏：城市显示与切换按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 当前城市显示
            Column {
                Text(
                    text = "当前城市",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = currentCity,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            // 切换城市按钮
            Button(
                onClick = { navController.navigate(Screen.CitySelection.route) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "选择城市",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("切换城市")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无天气数据")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitySelectionScreen(
    viewModel: WeatherViewModel,
    navController: NavHostController
) {
    var cityInput by remember { mutableStateOf("") }
    val popularCities by viewModel.popularCities.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("选择城市") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // 普通输入框
            OutlinedTextField(
                value = cityInput,
                onValueChange = { cityInput = it },
                label = { Text("请输入城市名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                ),
                visualTransformation = VisualTransformation.None
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 按钮行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 确认按钮
                Button(
                    onClick = {
                        if (cityInput.isNotBlank()) {
                            viewModel.addCityToPopular(cityInput)
                            cityInput = ""
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = cityInput.isNotBlank()
                ) {
                    Text("添加到列表")
                }

                // 删除按钮
                Button(
                    onClick = {
                        viewModel.removeLastPopularCity()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red.copy(alpha = 0.8f)
                    ),
                    enabled = popularCities.isNotEmpty()
                ) {
                    Text("删除一个")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 热门城市列表
            Text(
                text = "热门城市",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(popularCities) { city ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = {
                            viewModel.updateCity(city)
                            navController.popBackStack()
                        }
                    ) {
                        Text(
                            text = city,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WeatherContent(weatherData: WeatherResponse) {
    val result = weatherData.result
    val realtime = result.realtime
    val future = result.future

    // 控制动画显示的状态
    var isCardVisible by remember { mutableStateOf(false) }

    LaunchedEffect(weatherData) {
        // 每次数据更新时重新播放动画
        isCardVisible = false
        delay(50)
        isCardVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 第一部分：当前天气（带动画效果）
        AnimatedVisibility(
            visible = isCardVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 1000)) +
                    scaleIn(animationSpec = tween(
                        durationMillis = 800,
                        easing = FastOutSlowInEasing
                    ), initialScale = 0.8f),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(getWeatherColor(realtime.info))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(getWeatherIcon(realtime.info)),
                        contentDescription = "天气图标",
                        modifier = Modifier.size(80.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 添加切换动画状态
                    var showTemperature by remember { mutableStateOf(true) }

                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(1000) // 等待1秒
                            showTemperature = !showTemperature // 切换显示状态
                        }
                    }

                    // 温度显示（大字）
                    AnimatedVisibility(
                        visible = showTemperature,
                        enter = fadeIn(animationSpec = tween(1000)),
                        exit = fadeOut(animationSpec = tween(1000))
                    ) {
                        Text(
                            text = "${realtime.temperature}°C",
                            style = MaterialTheme.typography.displayLarge, // 使用更大的字体
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.animateContentSize() // 平滑的尺寸变化
                        )
                    }

                    // 天气描述显示（大字）
                    AnimatedVisibility(
                        visible = !showTemperature,
                        enter = fadeIn(animationSpec = tween(1000)),
                        exit = fadeOut(animationSpec = tween(1000))
                    ) {
                        Text(
                            text = realtime.info,
                            style = MaterialTheme.typography.displayLarge, // 使用更大的字体
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.animateContentSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.city,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "湿度: ${realtime.humidity}% | 风力: ${realtime.power}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
        // 第二部分：未来几天预报
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

        // 第三部分：详细天气预报
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
fun FutureWeatherCard(dayWeather: FutureWeather) {
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
            val dateParts = dayWeather.date.split("-")
            val simpleDate = if (dateParts.size >= 3) "${dateParts[1]}/${dateParts[2]}" else dayWeather.date
            Text(
                text = simpleDate,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
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
fun DetailedWeatherCard(dayWeather: FutureWeather) {
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
        weatherInfo.contains("晴") -> R.drawable.qing
        weatherInfo.contains("多云") -> R.drawable.duoyun
        weatherInfo.contains("阴") -> R.drawable.yin
        weatherInfo.contains("雨") -> R.drawable.yu
        weatherInfo.contains("雪") -> R.drawable.xue
        else -> R.drawable.xue
    }
}

// 根据天气类型获取背景色
fun getWeatherColor(weatherInfo: String): Color {
    return when {
        weatherInfo.contains("晴") -> Color(0xFFFFD700)
        weatherInfo.contains("多云") -> Color(0xFF87CEEB)
        weatherInfo.contains("阴") -> Color(0xFF778899)
        weatherInfo.contains("雨") -> Color(0xFF4682B4)
        weatherInfo.contains("雪") -> Color(0xFFF0F8FF)
        else -> Color(0xFF2196F3)
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherAppPreview() {
    MaterialTheme {
        WeatherContent(
            WeatherResponse(
                reason = "success",
                errorCode = 0,
                result = WeatherResult(
                    city = "北京",
                    realtime = RealtimeWeather(
                        temperature = "25",
                        humidity = "60",
                        info = "晴",
                        wid = "00",
                        direct = "北风",
                        power = "3级",
                        aqi = "45"
                    ),
                    future = listOf(
                        FutureWeather(
                            date = "2024-10-25",
                            temperature = "18/25℃",
                            weather = "晴",
                            wid = Wid("00", "00"),
                            direct = "北风"
                        ),
                        FutureWeather(
                            date = "2024-10-26",
                            temperature = "17/24℃",
                            weather = "多云",
                            wid = Wid("01", "01"),
                            direct = "东北风"
                        )
                    )
                )
            )
        )
    }
}