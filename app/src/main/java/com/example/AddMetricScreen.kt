package com.example

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.domain.MetricType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMetricScreen(
    navController: NavController,
    viewModel: HealthViewModel
) {
    var selectedType by remember { mutableStateOf(MetricType.BLOOD_PRESSURE) }
    var value1 by remember { mutableStateOf("") }
    var value2 by remember { mutableStateOf("") }
    var showDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Metric") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp)
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
        ) {
            
            ExposedDropdownMenuBox(
                expanded = showDropdown,
                onExpandedChange = { showDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedType.getDisplayName(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Metric Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                )
                ExposedDropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false }
                ) {
                    MetricType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.getDisplayName()) },
                            onClick = {
                                selectedType = type
                                showDropdown = false
                                value1 = ""
                                value2 = ""
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedType == MetricType.BLOOD_PRESSURE) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = value1,
                        onValueChange = { value1 = it },
                        label = { Text("Systolic (mmHg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = value2,
                        onValueChange = { value2 = it },
                        label = { Text("Diastolic (mmHg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                OutlinedTextField(
                    value = value1,
                    onValueChange = { value1 = it },
                    label = { Text("${selectedType.getDisplayName()} (${selectedType.getUnit()})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val v1 = value1.toDoubleOrNull()
                    val v2 = if (selectedType == MetricType.BLOOD_PRESSURE) value2.toDoubleOrNull() else null
                    
                    if (v1 != null && (selectedType != MetricType.BLOOD_PRESSURE || v2 != null)) {
                        viewModel.addMetric(selectedType, v1, v2)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = value1.isNotBlank() && (selectedType != MetricType.BLOOD_PRESSURE || value2.isNotBlank())
            ) {
                Text("Save Metric")
            }
        }
    }
}
