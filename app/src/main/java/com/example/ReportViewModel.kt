package com.example

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.AuthRepository
import com.example.domain.Report
import com.example.domain.ReportRepository
import com.example.domain.ReportResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReportViewModel(
    private val reportRepository: ReportRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val reports: StateFlow<List<Report>> = authRepository.currentUser
        .flatMapLatest { user ->
            if (user != null) {
                reportRepository.getReports(user.id)
            } else {
                flowOf(emptyList())
            }
        }.catch {
            _uiState.value = ReportUiState.Error("Failed to load reports")
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun uploadReport(uri: Uri, fileName: String, fileType: String) {
        val user = authRepository.currentUser.value
        if (user == null) {
            _uiState.value = ReportUiState.Error("Please login to upload reports.")
            return
        }

        _uiState.value = ReportUiState.Loading
        viewModelScope.launch {
            val result = reportRepository.uploadReport(user.id, uri, fileName, fileType)
            when (result) {
                is ReportResult.Success -> {
                    _uiState.value = ReportUiState.Success("Report uploaded successfully")
                }
                is ReportResult.Error -> {
                    _uiState.value = ReportUiState.Error(result.message)
                }
                is ReportResult.Loading -> { }
            }
        }
    }

    fun deleteReport(report: Report) {
        _uiState.value = ReportUiState.Loading
        viewModelScope.launch {
            val result = reportRepository.deleteReport(report)
            when (result) {
                is ReportResult.Success -> {
                    _uiState.value = ReportUiState.Success("Report deleted")
                }
                is ReportResult.Error -> {
                    _uiState.value = ReportUiState.Error(result.message)
                }
                is ReportResult.Loading -> { }
            }
        }
    }

    fun resetState() {
        _uiState.value = ReportUiState.Idle
    }

    companion object {
        fun provideFactory(reportRepository: ReportRepository, authRepository: AuthRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
                    return ReportViewModel(reportRepository, authRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

sealed class ReportUiState {
    object Idle : ReportUiState()
    object Loading : ReportUiState()
    data class Success(val message: String) : ReportUiState()
    data class Error(val message: String) : ReportUiState()
}
