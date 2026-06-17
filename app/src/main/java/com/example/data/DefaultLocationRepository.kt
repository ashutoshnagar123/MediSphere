package com.example.data

import android.annotation.SuppressLint
import android.content.Context
import com.example.domain.LocationRepository
import com.example.domain.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

class DefaultLocationRepository(private val context: Context) : LocationRepository {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): LocationResult {
        return try {
            val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            if (location != null) {
                LocationResult.Success(location.latitude, location.longitude)
            } else {
                LocationResult.Error("Location not available")
            }
        } catch (e: Exception) {
            LocationResult.Error(e.message ?: "Failed to get location")
        }
    }
}
