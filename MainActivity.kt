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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cardplay.data.DataSource
import com.example.cardplay.model.Topic

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
fun WeatherApp() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 第一部分：居中的正方形框（占屏幕2/6）
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)  // 使用权重分配空间
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2196F3))  // 蓝色背景代表天空
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 天气图标 - 使用第一个天气图片作为示例
                Image(
                    painter = painterResource(R.drawable.xue), // 使用现有的图片
                    contentDescription = "天气图标",
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
                // 当前温度
                Text(
                    text = "25°C",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                // 天气描述
                Text(
                    text = "晴朗",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                // 位置
                Text(
                    text = "北京",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 第二部分：左右滑动的实时天气卡片（占屏幕1/6）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)  // 使用权重分配空间
        ) {
            Text(
                text = "实时天气",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(DataSource.topics.take(5)) { topic ->  // 只显示前5个作为实时天气
                    LiveWeatherCard(topic)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 第三部分：上下滑动的天气卡片列表（占屏幕3/6）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(3f)  // 使用权重分配空间
        ) {
            Text(
                text = "天气类型",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            WeatherCardList()
        }
    }
}

@Composable
fun LiveWeatherCard(topic: Topic) {
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
            Image(
                painter = painterResource(id = topic.imageRes),
                contentDescription = stringResource(id = topic.name),
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = topic.name),
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${topic.availableCourses}°C",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun WeatherCardList() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxHeight()
    ) {
        items(DataSource.topics) { topic ->
            WeatherCard(topic)
        }
    }
}

@Composable
fun WeatherCard(topic: Topic) {
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
            // 天气图标
            Image(
                painter = painterResource(id = topic.imageRes),
                contentDescription = stringResource(id = topic.name),
                modifier = Modifier
                    .size(56.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 天气信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(id = topic.name),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_grain), // 保持原来的图标
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "温度: ${topic.availableCourses}°C",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "湿度: ${(topic.availableCourses % 40) + 50}%", // 示例湿度数据
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // 右侧箭头 - 使用文本代替图标
            Text(
                text = ">",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherAppPreview() {
    MaterialTheme {
        WeatherApp()
    }
}