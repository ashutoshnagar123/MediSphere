package com.example

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.domain.HealthcareService
import com.example.domain.ServiceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyServicesScreen(navController: NavController, viewModel: MapsViewModel) {
    val services by viewModel.nearbyServices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.fetchCurrentLocationAndServices()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Services") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (services.isEmpty()) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No services found or location unavailable.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                items(services) { service ->
                    ServiceItem(service)
                }
            }
        }
    }
}

@Composable
fun ServiceItem(service: HealthcareService) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(service.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(service.type.name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(service.address, style = MaterialTheme.typography.bodyMedium)
                service.distanceKm?.let {
                    Text("Distance: ${String.format("%.1f", it)} km", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = {
                val gmmIntentUri = Uri.parse("google.navigation:q=${service.latitude},${service.longitude}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                if (mapIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(mapIntent)
                } else {
                    // Fallback to browser
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${service.latitude},${service.longitude}"))
                    context.startActivity(browserIntent)
                }
            }) {
                Icon(Icons.Filled.Directions, contentDescription = "Directions", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
