package com.example.domain

interface NearbyServicesRepository {
    suspend fun getNearbyServices(latitude: Double, longitude: Double, type: ServiceType? = null): List<HealthcareService>
}
