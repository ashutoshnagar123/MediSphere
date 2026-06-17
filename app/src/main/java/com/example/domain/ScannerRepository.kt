package com.example.domain

import android.graphics.Bitmap

sealed class ScannerResult<out T> {
    data class Success<T>(val data: T) : ScannerResult<T>()
    data class Error(val message: String) : ScannerResult<Nothing>()
}

interface ScannerRepository {
    suspend fun analyzeImage(bitmap: Bitmap, rotationDegrees: Int): ScannerResult<MedicineInfo>
}

data class MedicineInfo(
    val rawText: String,
    val detectedName: String?,
    val detectedDosage: String?,
    val aiAnalysis: String? = null
)
