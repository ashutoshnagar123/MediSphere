package com.example.di

import android.content.Context
import com.example.data.FirebaseAuthRepository
import com.example.data.FirebaseReportRepository
import com.example.data.FirebaseAiAnalysisRepository
import com.example.domain.AuthRepository
import com.example.domain.ReportRepository
import com.example.domain.AiAnalysisRepository
import com.example.domain.ScannerRepository
import com.example.data.DefaultScannerRepository
import com.example.domain.HealthRepository
import com.example.data.DefaultHealthRepository

import com.example.domain.ReminderRepository
import com.example.data.DefaultReminderRepository
import com.example.domain.SettingsRepository
import com.example.data.DefaultSettingsRepository

import com.example.domain.LocationRepository
import com.example.data.DefaultLocationRepository
import com.example.domain.EmergencyRepository
import com.example.data.DefaultEmergencyRepository
import com.example.domain.NearbyServicesRepository
import com.example.data.DefaultNearbyServicesRepository

/**
 * Dependency Injection container for the application.
 * Note: Manual Dependency Injection is used here instead of Hilt. 
 * This is because the current version of the Android Gradle Plugin (AGP 9.1+) 
 * lacks the 'BaseExtension' required by the current Hilt plugin, causing build failures.
 * This container follows similar patterns to avoid Hilt-related compilation issues 
 * while maintaining a clean architecture.
 */
interface AppContainer {
    val authRepository: AuthRepository
    val reportRepository: ReportRepository
    val aiAnalysisRepository: AiAnalysisRepository
    val scannerRepository: ScannerRepository
    val healthRepository: HealthRepository
    val reminderRepository: ReminderRepository
    val settingsRepository: SettingsRepository
    val locationRepository: LocationRepository
    val emergencyRepository: EmergencyRepository
    val nearbyServicesRepository: NearbyServicesRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(context)
    }

    override val reportRepository: ReportRepository by lazy {
        FirebaseReportRepository(context)
    }

    override val aiAnalysisRepository: AiAnalysisRepository by lazy {
        FirebaseAiAnalysisRepository(context)
    }

    override val scannerRepository: ScannerRepository by lazy {
        DefaultScannerRepository(context)
    }

    override val healthRepository: HealthRepository by lazy {
        DefaultHealthRepository(context)
    }

    override val reminderRepository: ReminderRepository by lazy {
        DefaultReminderRepository(context)
    }

    override val settingsRepository: SettingsRepository by lazy {
        DefaultSettingsRepository(context)
    }

    override val locationRepository: LocationRepository by lazy {
        DefaultLocationRepository(context)
    }

    override val emergencyRepository: EmergencyRepository by lazy {
        DefaultEmergencyRepository(context)
    }

    override val nearbyServicesRepository: NearbyServicesRepository by lazy {
        DefaultNearbyServicesRepository()
    }
}

