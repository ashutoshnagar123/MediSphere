package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MetricDao {
    @Query("SELECT * FROM health_metrics ORDER BY date DESC")
    fun getAllMetrics(): Flow<List<MetricEntity>>

    @Query("SELECT * FROM health_metrics WHERE type = :type ORDER BY date DESC")
    fun getMetricsByType(type: String): Flow<List<MetricEntity>>

    @Query("SELECT * FROM health_metrics WHERE isSynced = 0")
    suspend fun getUnsyncedMetrics(): List<MetricEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetric(metric: MetricEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetrics(metrics: List<MetricEntity>)

    @Delete
    suspend fun deleteMetric(metric: MetricEntity)

    @Query("UPDATE health_metrics SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}
