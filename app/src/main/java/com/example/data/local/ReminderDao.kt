package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val dosage: String,
    val times: String, // comma separated
    val isEnabled: Boolean,
    val repeatDaily: Boolean,
    val isSynced: Boolean
)

@Entity(tableName = "reminder_history")
data class ReminderHistoryEntity(
    @PrimaryKey val id: String,
    val reminderId: String,
    val timestamp: Long,
    val status: String
)

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: String)
    
    @Query("SELECT * FROM reminders WHERE isSynced = 0")
    suspend fun getUnsyncedReminders(): List<ReminderEntity>
    
    @Query("UPDATE reminders SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
    
    @Query("DELETE FROM reminders")
    suspend fun clearAllReminders()
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderEntity>)

    @Query("SELECT * FROM reminder_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ReminderHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: ReminderHistoryEntity)
}
