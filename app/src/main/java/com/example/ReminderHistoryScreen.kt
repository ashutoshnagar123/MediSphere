package com.example

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.domain.DoseStatus
import com.example.domain.ReminderHistoryItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderHistoryScreen(
    navController: NavController,
    viewModel: ReminderViewModel,
    userId: String?
) {
    val history by viewModel.history.collectAsState()
    val reminders by viewModel.reminders.collectAsState() // needed to map reminder names

    LaunchedEffect(userId) {
        if (userId != null) {
            viewModel.loadData(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dose History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (history.isEmpty()) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No dose history.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                items(history) { item ->
                    val medName = reminders.find { it.id == item.reminderId }?.name ?: "Unknown Medicine"
                    HistoryItemView(item, medName)
                }
            }
        }
    }
}

@Composable
fun HistoryItemView(item: ReminderHistoryItem, medName: String) {
    val df = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val dateString = df.format(Date(item.timestamp))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(medName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Scheduled: $dateString", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                item.status.name,
                style = MaterialTheme.typography.labelLarge,
                color = when (item.status) {
                    DoseStatus.TAKEN -> MaterialTheme.colorScheme.primary
                    DoseStatus.MISSED -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.secondary
                }
            )
        }
    }
}
