package com.example

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.domain.HealthMetric
import com.example.domain.MetricType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDashboardScreen(
    navController: NavController,
    viewModel: HealthViewModel,
    userId: String?
) {
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncError by viewModel.syncError.collectAsState()
    val allMetrics by viewModel.allMetrics.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        if (userId != null) {
            viewModel.syncMetrics(userId)
        }
    }

    LaunchedEffect(syncError) {
        syncError?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Health Tracking") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (userId != null) {
                        IconButton(onClick = { viewModel.syncMetrics(userId) }) {
                            if (isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.Sync, contentDescription = "Sync")
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_metric") }) {
                Icon(Icons.Filled.Add, "Add Metric")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (allMetrics.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No records found. Click + to add a health metric.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(MetricType.values()) { type ->
                        val latest = allMetrics.filter { it.type == type }.maxByOrNull { it.date }
                        MetricCard(type = type, latest = latest) {
                            navController.navigate("metric_history/${type.name}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(type: MetricType, latest: HealthMetric?, onClick: () -> Unit) {
    val icon = getIconForMetric(type)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(type.getDisplayName(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (latest != null) {
                val valueText = if (latest.value2 != null) {
                    "${latest.value1.toInt()}/${latest.value2.toInt()}"
                } else {
                    String.format("%.1f", latest.value1)
                }
                Text("$valueText ${type.getUnit()}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                val formatter = java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault())
                Text("Last updated: ${formatter.format(java.util.Date(latest.date))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("--", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("No data", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

fun getIconForMetric(type: MetricType): ImageVector {
    return when(type) {
        MetricType.BLOOD_PRESSURE -> Icons.Filled.Favorite
        MetricType.BLOOD_SUGAR -> Icons.Filled.WaterDrop
        MetricType.WEIGHT -> Icons.Filled.MonitorWeight
        MetricType.BMI -> Icons.Filled.Scale
        MetricType.HEART_RATE -> Icons.Filled.Favorite
        MetricType.CHOLESTEROL -> Icons.Filled.WaterDrop // Just placeholder icon
    }
}
