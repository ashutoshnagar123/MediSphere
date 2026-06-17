package com.example.data

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.MetricEntity
import com.example.domain.HealthMetric
import com.example.domain.HealthRepository
import com.example.domain.MetricType
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class DefaultHealthRepository(private val context: Context) : HealthRepository {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java, "medisphere-database"
    ).build()

    private val metricDao = db.metricDao()
    
    private val isFirebaseInitialized = try {
        FirebaseApp.getApps(context).isNotEmpty() || FirebaseApp.initializeApp(context) != null
    } catch (e: Exception) {
        false
    }
    
    private val firestore by lazy { if (isFirebaseInitialized) FirebaseFirestore.getInstance() else null }

    override fun getAllMetrics(): Flow<List<HealthMetric>> {
        return metricDao.getAllMetrics().map { entities -> 
            entities.map { it.toDomain() }
        }
    }

    override fun getMetricsByType(type: MetricType): Flow<List<HealthMetric>> {
        return metricDao.getMetricsByType(type.name).map { entities -> 
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addMetric(metric: HealthMetric) {
        metricDao.insertMetric(metric.toEntity())
    }

    override suspend fun deleteMetric(metric: HealthMetric) {
        metricDao.deleteMetric(metric.toEntity())
        if (firestore != null) {
            // Best effort delete from cloud, assuming current user is available in calling context
            // or skipped for now as user id isn't passed to deleteMetric.
        }
    }

    override suspend fun syncWithCloud(userId: String) {
        if (firestore == null) return
        
        try {
            // 1. Upload unsynced local metrics
            val unsynced = metricDao.getUnsyncedMetrics()
            if (unsynced.isNotEmpty()) {
                val batch = firestore!!.batch()
                unsynced.forEach { entity ->
                    val docRef = firestore!!.collection("users").document(userId).collection("metrics").document(entity.id)
                    batch.set(docRef, entity.toMap())
                }
                batch.commit().await()
                metricDao.markAsSynced(unsynced.map { it.id })
            }

            // 2. Download all metrics from cloud
            val snapshot = firestore!!.collection("users").document(userId).collection("metrics").get().await()
            val cloudMetrics = snapshot.documents.mapNotNull { doc ->
                val typeStr = doc.getString("type") ?: return@mapNotNull null
                val value1 = doc.getDouble("value1") ?: return@mapNotNull null
                val value2 = doc.getDouble("value2")
                val date = doc.getLong("date") ?: return@mapNotNull null
                
                MetricEntity(
                    id = doc.id,
                    type = typeStr,
                    value1 = value1,
                    value2 = value2,
                    date = date,
                    isSynced = true
                )
            }
            if (cloudMetrics.isNotEmpty()) {
                metricDao.insertMetrics(cloudMetrics)
            }
        } catch (e: Exception) {
            // Handle sync error, could be logged or ignored
        }
    }

    private fun MetricEntity.toDomain() = HealthMetric(
        id = id,
        type = MetricType.valueOf(type),
        value1 = value1,
        value2 = value2,
        date = date,
        isSynced = isSynced
    )

    private fun HealthMetric.toEntity() = MetricEntity(
        id = id,
        type = type.name,
        value1 = value1,
        value2 = value2,
        date = date,
        isSynced = isSynced
    )

    private fun MetricEntity.toMap(): Map<String, Any?> = mapOf(
        "type" to type,
        "value1" to value1,
        "value2" to value2,
        "date" to date
    )
}
