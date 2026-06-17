package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        MetricEntity::class, 
        ReminderEntity::class, 
        ReminderHistoryEntity::class,
        EmergencyContactEntity::class
    ], 
    version = 3, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun metricDao(): MetricDao
    abstract fun reminderDao(): ReminderDao
    abstract fun emergencyContactDao(): EmergencyContactDao
}
