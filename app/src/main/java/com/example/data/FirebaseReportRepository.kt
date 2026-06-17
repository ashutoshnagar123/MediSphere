package com.example.data

import android.content.Context
import android.net.Uri
import com.example.domain.Report
import com.example.domain.ReportRepository
import com.example.domain.ReportResult
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseReportRepository(private val context: Context) : ReportRepository {

    private val isFirebaseInitialized = try {
        FirebaseApp.getApps(context).isNotEmpty() || FirebaseApp.initializeApp(context) != null
    } catch (e: Exception) {
        false
    }

    private val firestore by lazy { if (isFirebaseInitialized) FirebaseFirestore.getInstance() else null }
    private val storage by lazy { if (isFirebaseInitialized) FirebaseStorage.getInstance() else null }

    override fun getReports(userId: String): Flow<List<Report>> {
        if (!isFirebaseInitialized || firestore == null) {
            return flowOf(emptyList()) // Or a mock list of reports
        }

        return callbackFlow {
            val query = firestore!!.collection("reports")
                .whereEqualTo("userId", userId)
                .orderBy("uploadedAt", Query.Direction.DESCENDING)

            val listenerRegistration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val reports = snapshot.documents.mapNotNull { it.toObject(Report::class.java) }
                    trySend(reports)
                }
            }

            awaitClose { listenerRegistration.remove() }
        }
    }

    override suspend fun uploadReport(userId: String, uri: Uri, fileName: String, fileType: String): ReportResult<Report> {
        if (!isFirebaseInitialized || firestore == null || storage == null) {
            return ReportResult.Error("Firebase is not initialized")
        }

        return try {
            val reportId = UUID.randomUUID().toString()
            val storageRef = storage!!.reference.child("reports/$userId/$reportId-$fileName")
            
            // Upload file
            storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            val report = Report(
                reportId = reportId,
                userId = userId,
                fileUrl = downloadUrl,
                fileType = fileType,
                fileName = fileName,
                uploadedAt = System.currentTimeMillis()
            )

            // Save metadata
            firestore!!.collection("reports").document(reportId).set(report).await()

            ReportResult.Success(report)
        } catch (e: Exception) {
            ReportResult.Error(e.message ?: "Failed to upload report")
        }
    }

    override suspend fun deleteReport(report: Report): ReportResult<Unit> {
        if (!isFirebaseInitialized || firestore == null || storage == null) {
            return ReportResult.Error("Firebase is not initialized")
        }

        return try {
            // Delete metadata from firestore
            firestore!!.collection("reports").document(report.reportId).delete().await()
            
            // Delete file from storage
            val storageRef = storage!!.getReferenceFromUrl(report.fileUrl)
            storageRef.delete().await()

            ReportResult.Success(Unit)
        } catch (e: Exception) {
            ReportResult.Error(e.message ?: "Failed to delete report")
        }
    }

    override suspend fun updateReport(report: Report): ReportResult<Report> {
        if (!isFirebaseInitialized || firestore == null) {
            return ReportResult.Error("Firebase is not initialized")
        }

        return try {
            firestore!!.collection("reports").document(report.reportId).set(report).await()
            ReportResult.Success(report)
        } catch (e: Exception) {
            ReportResult.Error(e.message ?: "Failed to update report")
        }
    }
}
