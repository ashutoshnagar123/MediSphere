package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.HealthcareService
import com.example.domain.LocationRepository
import com.example.domain.LocationResult
import com.example.domain.NearbyServicesRepository
import com.example.domain.ServiceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapsViewModel(
    private val locationRepository: LocationRepository,
    private val nearbyServicesRepository: NearbyServicesRepository
) : ViewModel() {

    private val _location = MutableStateFlow<LocationResult?>(null)
    val location: StateFlow<LocationResult?> = _location.asStateFlow()

    private val _nearbyServices = MutableStateFlow<List<HealthcareService>>(emptyList())
    val nearbyServices: StateFlow<List<HealthcareService>> = _nearbyServices.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchCurrentLocationAndServices(type: ServiceType? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            val locResult = locationRepository.getCurrentLocation()
            _location.value = locResult

            if (locResult is LocationResult.Success) {
                val services = nearbyServicesRepository.getNearbyServices(locResult.latitude, locResult.longitude, type)
                _nearbyServices.value = services
            }
            _isLoading.value = false
        }
    }

    companion object {
        fun provideFactory(
            locRepo: LocationRepository, 
            nearbyRepo: NearbyServicesRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MapsViewModel(locRepo, nearbyRepo) as T
            }
        }
    }
}
