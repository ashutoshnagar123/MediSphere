package com.example.domain

data class Report(
    val reportId: String = "",
    val userId: String = "",
    val fileUrl: String = "",
    val fileType: String = "", // e.g. "PDF" or "IMAGE"
    val fileName: String = "",
    val uploadedAt: Long = 0L,
    val extractedText: String? = null,
    val analysisResult: String? = null
)
