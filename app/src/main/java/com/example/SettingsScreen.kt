package com.example

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings (Local)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsToggle(
                title = "Dark Mode",
                description = "Enable dark theme",
                checked = settings.isDarkMode,
                onCheckedChange = { viewModel.updateDarkMode(it) }
            )
            HorizontalDivider()
            SettingsToggle(
                title = "Notifications",
                description = "Enable local medicine reminders",
                checked = settings.notificationsEnabled,
                onCheckedChange = { viewModel.updateNotifications(it) }
            )
            HorizontalDivider()
            SettingsToggle(
                title = "Share Data for Research",
                description = "Opt in to help health discovery securely",
                checked = settings.shareDataForResearch,
                onCheckedChange = { viewModel.updateShareData(it) }
            )
            HorizontalDivider()
            // Language simplistic selector
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Language", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = settings.language == "en",
                        onClick = { viewModel.updateLanguage("en") }
                    )
                    Text("English")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = settings.language == "es",
                        onClick = { viewModel.updateLanguage("es") }
                    )
                    Text("Spanish")
                }
            }
        }
    }
}

@Composable
fun SettingsToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
