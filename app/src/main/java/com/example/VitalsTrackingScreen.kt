package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitalsTrackingScreen(navController: NavController) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /*TODO*/ },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Log")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Text("Vitals Tracking", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
                Text("Monitor your Blood Pressure", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Tabs/Filters
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha=0.5f)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Blood Pressure", color = MaterialTheme.colorScheme.onSecondaryContainer, style = MaterialTheme.typography.labelMedium)
                    }
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Blood Sugar", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                    }
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Heart Rate", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Chart Box
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        // Background lines
                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))
                        }
                        
                        // Fake data points for illustration
                        val pointsSys = listOf(Pair(0.2f, 0.4f), Pair(0.5f, 0.3f), Pair(0.8f, 0.35f))
                        val pointsDia = listOf(Pair(0.2f, 0.7f), Pair(0.5f, 0.65f), Pair(0.8f, 0.7f))

                        pointsSys.forEach { p ->
                            Box(modifier = Modifier.align(BiasAlignment(p.first * 2 - 1, p.second * 2 - 1)).size(8.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)))
                        }
                        pointsDia.forEach { p ->
                            Box(modifier = Modifier.align(BiasAlignment(p.first * 2 - 1, p.second * 2 - 1)).size(8.dp).background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(4.dp)))
                        }

                        // Legend
                        Column(modifier = Modifier.align(Alignment.TopEnd)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Systolic", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(4.dp)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Diastolic", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        // X-axis
                        Row(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Oct 1", style = MaterialTheme.typography.labelSmall)
                            Text("Oct 4", style = MaterialTheme.typography.labelSmall)
                            Text("Oct 7", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // AI Insight
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha=0.1f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha=0.3f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("AI Health Insight", style = MaterialTheme.typography.labelLarge)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Your BP has been stable over the last 7 days, averaging 118/78 mmHg. This is within the optimal range. Keep up the good work!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Recent Readings
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Recent Readings", style = MaterialTheme.typography.headlineSmall)
                    
                    VitalLogItem(value = "118 / 78 mmHg", time = "Today, 8:00 AM", status = "Optimal", statusColor = MaterialTheme.colorScheme.tertiaryContainer, bgColor = MaterialTheme.colorScheme.tertiaryContainer.copy(0.1f))
                    VitalLogItem(value = "120 / 80 mmHg", time = "Yesterday, 7:30 AM", status = "Normal", statusColor = MaterialTheme.colorScheme.tertiary, bgColor = MaterialTheme.colorScheme.tertiary.copy(0.1f))
                    VitalLogItem(value = "125 / 82 mmHg", time = "Oct 5, 8:15 AM", status = "Elevated", statusColor = MaterialTheme.colorScheme.onSurfaceVariant, bgColor = MaterialTheme.colorScheme.surfaceVariant)

                    OutlinedButton(
                        onClick = { /*TODO*/ },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("View Full History")
                    }
                }
            }
        }
    }
}

@Composable
fun VitalLogItem(value: String, time: String, status: String, statusColor: Color, bgColor: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(0.5f), RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(value, style = MaterialTheme.typography.labelLarge)
                    Text(time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Surface(color = bgColor, shape = RoundedCornerShape(4.dp)) {
                Text(status, style = MaterialTheme.typography.labelMedium, color = statusColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}
