package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.example.data.local.AppDatabase
import com.example.data.local.ReminderEntity
import com.example.data.local.ReminderHistoryEntity
import com.example.domain.DoseStatus
import com.example.domain.MedicineReminder
import com.example.domain.ReminderHistoryItem
import com.example.domain.ReminderRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import com.example.worker.ReminderWorker

class DefaultReminderRepository(private val context: Context) : ReminderRepository {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java, "medisphere-database"
    ).fallbackToDestructiveMigration(dropAllTables = true).build()

    private val reminderDao = db.reminderDao()
    private val workManager = WorkManager.getInstance(context)

    private val isFirebaseInitialized = try {
        FirebaseApp.getApps(context).isNotEmpty() || FirebaseApp.initializeApp(context) != null
    } catch (e: Exception) {
        false
    }

    private val firestore by lazy { if (isFirebaseInitialized) FirebaseFirestore.getInstance() else null }

    init {
        // Schedule worker to check reminders periodically
        val checkRemindersRequest = PeriodicWorkRequestBuilder<ReminderWorker>(15, TimeUnit.MINUTES)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "CheckReminders",
            ExistingPeriodicWorkPolicy.KEEP,
            checkRemindersRequest
        )
    }

    override suspend fun getReminders(userId: String): Flow<List<MedicineReminder>> {
        syncWithCloud(userId)
        return reminderDao.getAllReminders().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addReminder(userId: String, reminder: MedicineReminder) {
        reminderDao.insertReminder(reminder.toEntity())
        syncWithCloud(userId)
    }

    override suspend fun updateReminder(userId: String, reminder: MedicineReminder) {
        reminderDao.insertReminder(reminder.toEntity())
        syncWithCloud(userId)
    }

    override suspend fun deleteReminder(userId: String, reminderId: String) {
        reminderDao.deleteReminderById(reminderId)
        firestore?.collection("users")?.document(userId)?.collection("reminders")?.document(reminderId)?.delete()
    }

    override suspend fun getHistory(userId: String): Flow<List<ReminderHistoryItem>> {
        return reminderDao.getAllHistory().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addHistoryItem(userId: String, item: ReminderHistoryItem) {
        reminderDao.insertHistory(item.toEntity())
    }

    override suspend fun updateHistoryItem(userId: String, item: ReminderHistoryItem) {
        reminderDao.insertHistory(item.toEntity())
    }

    private suspend fun syncWithCloud(userId: String) {
        if (firestore == null) return
        try {
            val unsynced = reminderDao.getUnsyncedReminders()
            if (unsynced.isNotEmpty()) {
                val batch = firestore!!.batch()
                unsynced.forEach { entity ->
                    val docRef = firestore!!.collection("users").document(userId).collection("reminders").document(entity.id)
                    batch.set(docRef, entity.toMap())
                }
                batch.commit().await()
                reminderDao.markAsSynced(unsynced.map { it.id })
            }

            val snapshot = firestore!!.collection("users").document(userId).collection("reminders").get().await()
            val cloudReminders = snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                val dosage = doc.getString("dosage") ?: return@mapNotNull null
                val times = (doc.get("times") as? List<*>)?.filterIsInstance<String>() ?: return@mapNotNull null
                val isEnabled = doc.getBoolean("isEnabled") ?: true
                val repeatDaily = doc.getBoolean("repeatDaily") ?: true

                ReminderEntity(
                    id = doc.id,
                    name = name,
                    dosage = dosage,
                    times = times.joinToString(","),
                    isEnabled = isEnabled,
                    repeatDaily = repeatDaily,
                    isSynced = true
                )
            }
            if (cloudReminders.isNotEmpty()) {
                reminderDao.clearAllReminders()
                reminderDao.insertReminders(cloudReminders)
            }
        } catch (e: Exception) {
            // log or ignore
        }
    }

    private fun ReminderEntity.toDomain() = MedicineReminder(
        id = id,
        name = name,
        dosage = dosage,
        times = times.split(",").filter { it.isNotBlank() },
        isEnabled = isEnabled,
        repeatDaily = repeatDaily,
        isSynced = true
    )

    private fun MedicineReminder.toEntity() = ReminderEntity(
        id = id,
        name = name,
        dosage = dosage,
        times = times.joinToString(","),
        isEnabled = isEnabled,
        repeatDaily = repeatDaily,
        isSynced = isSynced
    )

    private fun ReminderEntity.toMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "dosage" to dosage,
        "times" to times.split(",").filter { it.isNotBlank() },
        "isEnabled" to isEnabled,
        "repeatDaily" to repeatDaily
    )

    private fun ReminderHistoryEntity.toDomain() = ReminderHistoryItem(
        id = id,
        reminderId = reminderId,
        timestamp = timestamp,
        status = DoseStatus.valueOf(status)
    )

    private fun ReminderHistoryItem.toEntity() = ReminderHistoryEntity(
        id = id,
        reminderId = reminderId,
        timestamp = timestamp,
        status = status.name
    )
}
