package com.example.domain

sealed class ReportResult<out T> {
    data class Success<out T>(val data: T) : ReportResult<T>()
    data class Error(val message: String) : ReportResult<Nothing>()
    object Loading : ReportResult<Nothing>()
}
