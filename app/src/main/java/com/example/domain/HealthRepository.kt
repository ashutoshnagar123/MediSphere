package com.example.domain

import kotlinx.coroutines.flow.Flow

interface HealthRepository {
    fun getAllMetrics(): Flow<List<HealthMetric>>
    fun getMetricsByType(type: MetricType): Flow<List<HealthMetric>>
    suspend fun addMetric(metric: HealthMetric)
    suspend fun deleteMetric(metric: HealthMetric)
    suspend fun syncWithCloud(userId: String)
}
