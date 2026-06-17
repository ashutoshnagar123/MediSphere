package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.MetricType

@Entity(tableName = "health_metrics")
data class MetricEntity(
    @PrimaryKey val id: String,
    val type: String,
    val value1: Double,
    val value2: Double?,
    val date: Long,
    val isSynced: Boolean
)
