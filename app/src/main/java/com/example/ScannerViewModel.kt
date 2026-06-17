package com.example

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.ScannerRepository
import com.example.domain.ScannerResult
import com.example.domain.MedicineInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.domain.AuthRepository
import com.example.domain.ReportRepository
import com.example.domain.ReportResult
import java.io.File
import java.io.FileOutputStream
import android.content.Context
import androidx.core.net.toUri

// import GeminiRepository
import com.example.GeminiRepository

class ScannerViewModel(
    private val scannerRepository: ScannerRepository,
    private val reportRepository: ReportRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()
    private val geminiRepository = GeminiRepository()

    fun analyzeImage(bitmap: Bitmap, rotationDegrees: Int, scanType: String, context: Context) {
        _uiState.value = ScannerUiState.Loading("Analyzing image...")
        viewModelScope.launch {
            val result = scannerRepository.analyzeImage(bitmap, rotationDegrees)
            when (result) {
                is ScannerResult.Success -> {
                    if (scanType == "report") {
                        val userId = authRepository.currentUser.value?.id
                        if (userId != null) {
                            // Save bitmap to temp file
                            val file = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
                            val out = FileOutputStream(file)
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                            out.flush()
                            out.close()
                            
                            val uploadRes = reportRepository.uploadReport(userId, file.toUri(), file.name, "IMAGE")
                            if (uploadRes is ReportResult.Success) {
                                val updatedReport = uploadRes.data.copy(extractedText = result.data.rawText)
                                reportRepository.updateReport(updatedReport)
                                _uiState.value = ScannerUiState.ReportSuccess(updatedReport.reportId)
                            } else {
                                _uiState.value = ScannerUiState.Error("Failed to upload report")
                            }
                        } else {
                            _uiState.value = ScannerUiState.Error("User not logged in")
                        }
                    } else {
                        // Scan Type is Medicine
                        val medicineName = result.data.detectedName
                        var enrichedMedicine = result.data
                        if (!medicineName.isNullOrBlank()) {
                            _uiState.value = ScannerUiState.Loading("AI is finding information for $medicineName...")
                            val prompt = "Provide a short paragraph summarizing the uses, common side effects, and precautions for the medicine: $medicineName. Keep it brief and strictly medical."
                            val aiInfo = geminiRepository.generateContent(prompt)
                            enrichedMedicine = enrichedMedicine.copy(aiAnalysis = aiInfo)
                        }
                        _uiState.value = ScannerUiState.Success(enrichedMedicine)
                    }
                }
                is ScannerResult.Error -> {
                    _uiState.value = ScannerUiState.Error(result.message)
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = ScannerUiState.Idle
    }

    companion object {
        fun provideFactory(
            scannerRepository: ScannerRepository,
            reportRepository: ReportRepository,
            authRepository: AuthRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ScannerViewModel::class.java)) {
                    return ScannerViewModel(scannerRepository, reportRepository, authRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

sealed class ScannerUiState {
    object Idle : ScannerUiState()
    data class Loading(val message: String) : ScannerUiState()
    data class Success(val medicineInfo: MedicineInfo) : ScannerUiState()
    data class ReportSuccess(val reportId: String) : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
}
