package com.example

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.domain.HealthMetric
import com.example.domain.MetricType
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricHistoryScreen(
    navController: NavController,
    viewModel: HealthViewModel,
    metricTypeStr: String
) {
    val metricType = try { MetricType.valueOf(metricTypeStr) } catch(e: Exception) { MetricType.WEIGHT }
    
    val metrics by viewModel.getMetricsByType(metricType).collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Weekly, 1: Monthly (using mock filtering for now)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${metricType.getDisplayName()} History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Recent") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("All Time") })
            }

            if (metrics.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No history available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val chartData = metrics.sortedBy { it.date }
                
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(16.dp).fillMaxWidth().height(200.dp)
                ) {
                    if (metricType == MetricType.BLOOD_PRESSURE) {
                        SimpleLineChart(
                            data1 = chartData.map { it.value1.toFloat() }, 
                            data2 = chartData.mapNotNull { it.value2?.toFloat() }
                        )
                    } else {
                        SimpleLineChart(data1 = chartData.map { it.value1.toFloat() })
                    }
                }

                Text(
                    "History Log", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(metrics) { metric ->
                        MetricHistoryItem(metric = metric, type = metricType, onDelete = { viewModel.deleteMetric(it) })
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleLineChart(data1: List<Float>, data2: List<Float> = emptyList()) {
    if (data1.isEmpty()) return
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.error

    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val width = size.width
        val height = size.height

        val maxVal = maxOf(data1.maxOrNull() ?: 0f, data2.maxOrNull() ?: 0f) * 1.2f
        val minVal = minOf(data1.minOrNull() ?: 0f, data2.minOrNull() ?: 0f) * 0.8f
        val range = if (maxVal - minVal == 0f) 1f else maxVal - minVal

        fun getX(index: Int) = if (data1.size > 1) index * (width / (data1.size - 1)) else width / 2
        fun getY(value: Float) = height - ((value - minVal) / range) * height

        val path1 = Path()
        data1.forEachIndexed { index, value ->
            if (index == 0) path1.moveTo(getX(index), getY(value))
            else path1.lineTo(getX(index), getY(value))
            drawCircle(color = primaryColor, radius = 6f, center = Offset(getX(index), getY(value)))
        }
        drawPath(path = path1, color = primaryColor, style = Stroke(width = 4f, cap = StrokeCap.Round))

        if (data2.isNotEmpty()) {
            val path2 = Path()
            data2.forEachIndexed { index, value ->
                if (index == 0) path2.moveTo(getX(index), getY(value))
                else path2.lineTo(getX(index), getY(value))
                drawCircle(color = secondaryColor, radius = 6f, center = Offset(getX(index), getY(value)))
            }
            drawPath(path = path2, color = secondaryColor, style = Stroke(width = 4f, cap = StrokeCap.Round))
        }
    }
}

@Composable
fun MetricHistoryItem(metric: HealthMetric, type: MetricType, onDelete: (HealthMetric) -> Unit) {
    val formatter = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
    
    ListItem(
        headlineContent = {
            val valueText = if (metric.value2 != null) {
                "${metric.value1.toInt()}/${metric.value2.toInt()} ${type.getUnit()}"
            } else {
                String.format("%.1f ${type.getUnit()}", metric.value1)
            }
            Text(valueText, fontWeight = FontWeight.Bold)
        },
        supportingContent = {
            Text(formatter.format(java.util.Date(metric.date)))
        },
        trailingContent = {
            IconButton(onClick = { onDelete(metric) }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}
