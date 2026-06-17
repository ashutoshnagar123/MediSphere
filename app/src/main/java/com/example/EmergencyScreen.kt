package com.example

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.domain.LocationResult
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EmergencyScreen(navController: NavController, mapsViewModel: MapsViewModel) {
    val locationState by mapsViewModel.location.collectAsState()
    val context = LocalContext.current

    val callPermissionState = rememberPermissionState(Manifest.permission.CALL_PHONE)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency SOS") },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { 
                    if (callPermissionState.status.isGranted) {
                        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:911"))
                        context.startActivity(intent)
                    } else {
                        callPermissionState.launchPermissionRequest()
                    }
                },
                shape = CircleShape,
                modifier = Modifier.size(200.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Warning, contentDescription = "SOS", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onError)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("SOS", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onError)
                    Text("Call 911", color = MaterialTheme.colorScheme.onError)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { navController.navigate("emergency_contacts") },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Manage Emergency Contacts")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("nearby_services") },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Find Nearby Hospitals")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { navController.navigate("maps") },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("View Map")
            }
        }
    }
}
