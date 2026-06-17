package com.example.domain

import java.util.UUID

data class HealthcareService(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: ServiceType,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double? = null,
    val phone: String? = null
)

enum class ServiceType {
    HOSPITAL,
    CLINIC,
    PHARMACY,
    DIAGNOSTIC,
    AMBULANCE
}
