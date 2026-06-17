package com.example.data

import com.example.domain.HealthcareService
import com.example.domain.NearbyServicesRepository
import com.example.domain.ServiceType
import kotlinx.coroutines.delay
import java.util.UUID

class DefaultNearbyServicesRepository : NearbyServicesRepository {
    override suspend fun getNearbyServices(latitude: Double, longitude: Double, type: ServiceType?): List<HealthcareService> {
        // In a real app, you would call Google Places API here.
        // For demonstration purposes without Places API, we'll return a few mocked locations nearby.
        delay(1000)
        
        val services = listOf(
            HealthcareService(
                id = UUID.randomUUID().toString(),
                name = "City Central Hospital",
                type = ServiceType.HOSPITAL,
                address = "123 Main St, Metro",
                latitude = latitude + 0.01,
                longitude = longitude + 0.01,
                distanceKm = 1.2,
                phone = "+1234567890"
            ),
            HealthcareService(
                id = UUID.randomUUID().toString(),
                name = "Green Cross Clinic",
                type = ServiceType.CLINIC,
                address = "45 Health Ave",
                latitude = latitude - 0.005,
                longitude = longitude + 0.008,
                distanceKm = 0.8,
                phone = "+1987654321"
            ),
            HealthcareService(
                id = UUID.randomUUID().toString(),
                name = "Quick Aid Pharmacy",
                type = ServiceType.PHARMACY,
                address = "88 Life Blvd",
                latitude = latitude + 0.015,
                longitude = longitude - 0.01,
                distanceKm = 2.0,
                phone = "+1122334455"
            ),
            HealthcareService(
                id = UUID.randomUUID().toString(),
                name = "Metro Ambulance Service",
                type = ServiceType.AMBULANCE,
                address = "99 Fast Ln",
                latitude = latitude - 0.02,
                longitude = longitude - 0.02,
                distanceKm = 3.5,
                phone = "911"
            )
        )
        return if (type == null) services else services.filter { it.type == type }
    }
}
