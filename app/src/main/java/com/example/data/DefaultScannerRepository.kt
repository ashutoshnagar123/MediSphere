package com.example.data

import android.content.Context
import android.graphics.Bitmap
import com.example.domain.MedicineInfo
import com.example.domain.ScannerRepository
import com.example.domain.ScannerResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class DefaultScannerRepository(private val context: Context) : ScannerRepository {
    override suspend fun analyzeImage(bitmap: Bitmap, rotationDegrees: Int): ScannerResult<MedicineInfo> {
        return try {
            val image = InputImage.fromBitmap(bitmap, rotationDegrees)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val result = recognizer.process(image).await()
            
            if (result.text.isBlank()) {
                return ScannerResult.Error("No text found in the image")
            }
            
            val lines = result.text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
            
            // Simple heuristic to find medicine name and dosage
            var detectedName: String? = null
            var detectedDosage: String? = null
            
            val dosageRegex = Regex("""\b(\d+(\.\d+)?\s*(mg|g|mcg|ml|iu|units))\b""", RegexOption.IGNORE_CASE)
            
            for (line in lines) {
                val match = dosageRegex.find(line)
                if (match != null) {
                    detectedDosage = match.value
                    // The name might be the same line before the dosage or the previous line
                    val namePart = line.replace(match.value, "").trim()
                    if (namePart.isNotEmpty()) {
                        detectedName = namePart
                    }
                    break
                }
            }
            
            // Fallback: If no dosage found, just take the first prominent block of text as name
            if (detectedName == null && lines.isNotEmpty()) {
                detectedName = lines.firstOrNull { it.length > 3 }
            }

            ScannerResult.Success(
                MedicineInfo(
                    rawText = result.text,
                    detectedName = detectedName,
                    detectedDosage = detectedDosage
                )
            )
        } catch (e: Exception) {
            ScannerResult.Error(e.message ?: "Failed to process image")
        }
    }
}
