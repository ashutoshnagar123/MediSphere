package com.example

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.AiAnalysisRepository
import com.example.domain.AnalysisSummary
import com.example.domain.ReportResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AiAnalysisViewModel(
    private val aiAnalysisRepository: AiAnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AiAnalysisUiState>(AiAnalysisUiState.Idle)
    val uiState: StateFlow<AiAnalysisUiState> = _uiState.asStateFlow()

    fun analyzeReport(reportId: String, uri: Uri) {
        _uiState.value = AiAnalysisUiState.Loading("Extracting text from image...")
        viewModelScope.launch {
            // STEP 1: Extract Text
            val textResult = aiAnalysisRepository.extractTextFromImage(uri)
            if (textResult is ReportResult.Error) {
                _uiState.value = AiAnalysisUiState.Error(textResult.message)
                return@launch
            }
            
            val extractedText = (textResult as ReportResult.Success).data
            
            // STEP 2: Analyze Text with Gemini
            _uiState.value = AiAnalysisUiState.Loading("Analyzing report with Gemini AI...")
            val analysisResult = aiAnalysisRepository.analyzeReport(extractedText)
            
            if (analysisResult is ReportResult.Error) {
                _uiState.value = AiAnalysisUiState.Error(analysisResult.message)
                return@launch
            }
            
            val summary = (analysisResult as ReportResult.Success).data
            
            // STEP 3: Save results to Firestore
            val jsonObj = org.json.JSONObject()
            jsonObj.put("explanation", summary.explanation)
            jsonObj.put("healthSummary", summary.healthSummary)
            jsonObj.put("specialist", summary.specialist)
            jsonObj.put("recommendations", summary.recommendations)
            val abnormalsArray = org.json.JSONArray()
            summary.abnormalValues.forEach { abnormalsArray.put(it) }
            jsonObj.put("abnormalValues", abnormalsArray)
            
            aiAnalysisRepository.saveAnalysis(reportId, extractedText, jsonObj.toString())
            
            _uiState.value = AiAnalysisUiState.Success(summary, extractedText)
        }
    }
    
    fun loadExistingAnalysis(analysisJson: String, extractedText: String) {
        try {
            val jsonObj = org.json.JSONObject(analysisJson)
            val abnormalArray = jsonObj.optJSONArray("abnormalValues")
            val abnormals = mutableListOf<String>()
            if (abnormalArray != null) {
                for (i in 0 until abnormalArray.length()) {
                    abnormals.add(abnormalArray.getString(i))
                }
            }
            val summary = AnalysisSummary(
                explanation = jsonObj.optString("explanation", "N/A"),
                abnormalValues = abnormals,
                healthSummary = jsonObj.optString("healthSummary", "N/A"),
                specialist = jsonObj.optString("specialist", "N/A"),
                recommendations = jsonObj.optString("recommendations", "N/A")
            )
            _uiState.value = AiAnalysisUiState.Success(summary, extractedText)
        } catch(e: Exception) {
             _uiState.value = AiAnalysisUiState.Error("Failed to parse existing analysis")
        }
    }

    fun resetState() {
        _uiState.value = AiAnalysisUiState.Idle
    }

    companion object {
        fun provideFactory(aiAnalysisRepository: AiAnalysisRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AiAnalysisViewModel::class.java)) {
                    return AiAnalysisViewModel(aiAnalysisRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

sealed class AiAnalysisUiState {
    object Idle : AiAnalysisUiState()
    data class Loading(val message: String) : AiAnalysisUiState()
    data class Success(val summary: AnalysisSummary, val extractedText: String) : AiAnalysisUiState()
    data class Error(val message: String) : AiAnalysisUiState()
}
