package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.HealthMetric
import com.example.domain.HealthRepository
import com.example.domain.MetricType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class HealthViewModel(
    private val repository: HealthRepository
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    val allMetrics = repository.getAllMetrics().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getMetricsByType(type: MetricType): StateFlow<List<HealthMetric>> {
        return repository.getMetricsByType(type).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun addMetric(type: MetricType, value1: Double, value2: Double?) {
        viewModelScope.launch {
            val metric = HealthMetric(
                id = UUID.randomUUID().toString(),
                type = type,
                value1 = value1,
                value2 = value2,
                date = System.currentTimeMillis()
            )
            repository.addMetric(metric)
        }
    }

    fun deleteMetric(metric: HealthMetric) {
        viewModelScope.launch {
            repository.deleteMetric(metric)
        }
    }

    fun syncMetrics(userId: String) {
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            _syncError.value = null
            try {
                repository.syncWithCloud(userId)
            } catch (e: Exception) {
                _syncError.value = "Sync failed: ${e.message}"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    companion object {
        fun provideFactory(repository: HealthRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(HealthViewModel::class.java)) {
                    return HealthViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
