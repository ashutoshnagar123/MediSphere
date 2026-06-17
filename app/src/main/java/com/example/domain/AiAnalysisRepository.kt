package com.example.domain

import android.net.Uri

interface AiAnalysisRepository {
    suspend fun extractTextFromImage(uri: Uri): ReportResult<String>
    suspend fun analyzeReport(text: String): ReportResult<AnalysisSummary>
    suspend fun saveAnalysis(reportId: String, extractedText: String, analysisResult: String): ReportResult<Unit>
}

data class AnalysisSummary(
    val explanation: String,
    val abnormalValues: List<String>,
    val healthSummary: String,
    val specialist: String,
    val recommendations: String
)
