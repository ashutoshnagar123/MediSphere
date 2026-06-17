package com.example.domain

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun getReports(userId: String): Flow<List<Report>>
    suspend fun uploadReport(userId: String, uri: Uri, fileName: String, fileType: String): ReportResult<Report>
    suspend fun deleteReport(report: Report): ReportResult<Unit>
    suspend fun updateReport(report: Report): ReportResult<Report>
}
