package com.example.domain

enum class MetricType {
    BLOOD_PRESSURE,
    BLOOD_SUGAR,
    WEIGHT,
    BMI,
    HEART_RATE,
    CHOLESTEROL;
    
    fun getDisplayName(): String = when(this) {
        BLOOD_PRESSURE -> "Blood Pressure"
        BLOOD_SUGAR -> "Blood Sugar"
        WEIGHT -> "Weight"
        BMI -> "BMI"
        HEART_RATE -> "Heart Rate"
        CHOLESTEROL -> "Cholesterol"
    }
    
    fun getUnit(): String = when(this) {
        BLOOD_PRESSURE -> "mmHg"
        BLOOD_SUGAR -> "mg/dL"
        WEIGHT -> "kg"
        BMI -> ""
        HEART_RATE -> "bpm"
        CHOLESTEROL -> "mg/dL"
    }
}

data class HealthMetric(
    val id: String,
    val type: MetricType,
    val value1: Double,
    val value2: Double? = null,
    val date: Long,
    val isSynced: Boolean = false
)
