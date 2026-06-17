package com.example

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.domain.LocationResult
import com.example.domain.HealthcareService
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapsScreen(navController: NavController, viewModel: MapsViewModel) {
    val locationState by viewModel.location.collectAsState()
    val services by viewModel.nearbyServices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            viewModel.fetchCurrentLocationAndServices()
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Healthcare Map") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (!locationPermissionState.status.isGranted) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Location permission required to show map.")
            }
        } else if (isLoading || locationState == null) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val loc = locationState as? LocationResult.Success
            if (loc != null) {
                val userLocation = LatLng(loc.latitude, loc.longitude)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(userLocation, 14f)
                }
                
                var properties by remember {
                    mutableStateOf(MapProperties(isMyLocationEnabled = true, mapType = MapType.NORMAL))
                }
                val uiSettings by remember {
                    mutableStateOf(MapUiSettings(zoomControlsEnabled = true))
                }

                GoogleMap(
                    modifier = Modifier.padding(innerPadding).fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = properties,
                    uiSettings = uiSettings
                ) {
                    services.forEach { service ->
                        Marker(
                            state = MarkerState(position = LatLng(service.latitude, service.longitude)),
                            title = service.name,
                            snippet = service.type.name
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Failed to retrieve location.")
                }
            }
        }
    }
}
