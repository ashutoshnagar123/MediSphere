package com.example.domain

import java.util.UUID

data class MedicineReminder(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dosage: String,
    val times: List<String>, // "HH:mm" format
    val isEnabled: Boolean = true,
    val repeatDaily: Boolean = true,
    val isSynced: Boolean = false // for firestore
)

data class ReminderHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val reminderId: String,
    val timestamp: Long, // when it was scheduled
    val status: DoseStatus
)

enum class DoseStatus {
    TAKEN, MISSED, SKIPPED, PENDING
}

interface ReminderRepository {
    suspend fun getReminders(userId: String): kotlinx.coroutines.flow.Flow<List<MedicineReminder>>
    suspend fun addReminder(userId: String, reminder: MedicineReminder)
    suspend fun updateReminder(userId: String, reminder: MedicineReminder)
    suspend fun deleteReminder(userId: String, reminderId: String)
    
    suspend fun getHistory(userId: String): kotlinx.coroutines.flow.Flow<List<ReminderHistoryItem>>
    suspend fun addHistoryItem(userId: String, item: ReminderHistoryItem)
    suspend fun updateHistoryItem(userId: String, item: ReminderHistoryItem)
}
