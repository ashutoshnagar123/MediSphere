package com.example.data

import android.content.Context
import android.net.Uri
import com.example.domain.AiAnalysisRepository
import com.example.domain.AnalysisSummary
import com.example.domain.ReportResult
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.ai.client.generativeai.GenerativeModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class FirebaseAiAnalysisRepository(private val context: Context) : AiAnalysisRepository {

    private val isFirebaseInitialized = try {
        FirebaseApp.getApps(context).isNotEmpty() || FirebaseApp.initializeApp(context) != null
    } catch (e: Exception) {
        false
    }

    private val firestore by lazy { if (isFirebaseInitialized) FirebaseFirestore.getInstance() else null }
    private val generativeModel = GenerativeModel(
        modelName = "gemini-3.1-pro-preview",
        apiKey = com.example.BuildConfig.GEMINI_API_KEY
    )

    override suspend fun extractTextFromImage(uri: Uri): ReportResult<String> {
        return try {
            val image = withContext(Dispatchers.IO) {
                if (uri.scheme == "http" || uri.scheme == "https") {
                    val url = URL(uri.toString())
                    val connection = url.openConnection()
                    connection.doInput = true
                    connection.connect()
                    val input = connection.inputStream
                    val bitmap = BitmapFactory.decodeStream(input)
                    if (bitmap != null) InputImage.fromBitmap(bitmap, 0) else null
                } else {
                    InputImage.fromFilePath(context, uri)
                }
            }
            
            if (image == null) return ReportResult.Error("Could not load image")

            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val result = recognizer.process(image).await()
            if (result.text.isBlank()) {
                ReportResult.Error("No text found in the image")
            } else {
                ReportResult.Success(result.text)
            }
        } catch (e: Exception) {
            ReportResult.Error(e.message ?: "Failed to extract text from image")
        }
    }

    override suspend fun analyzeReport(text: String): ReportResult<AnalysisSummary> {
        return try {
            val prompt = """
                Analyze the following medical report text and provide a structured JSON response.
                Never provide a diagnosis. Never replace professional medical advice.
                
                The JSON should have these precise keys:
                "explanation": Brief explanation of the report in simple language.
                "abnormalValues": Array of strings highlighting any abnormal values found.
                "healthSummary": General health summary based on the report.
                "specialist": Suggested appropriate specialist to consult.
                "recommendations": Diet and lifestyle recommendations.
                
                Report Text:
                $text
            """.trimIndent()
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text ?: return ReportResult.Error("Empty AI response")
            
            // Clean up the JSON response
            val cleanJson = responseText.replace("```json", "").replace("```", "").trim()
            val jsonObj = JSONObject(cleanJson)
            
            val abnormalArray = jsonObj.optJSONArray("abnormalValues")
            val abnormals = mutableListOf<String>()
            if (abnormalArray != null) {
                for (i in 0 until abnormalArray.length()) {
                    abnormals.add(abnormalArray.getString(i))
                }
            }
            
            val summary = AnalysisSummary(
                explanation = jsonObj.optString("explanation", "N/A"),
                abnormalValues = abnormals,
                healthSummary = jsonObj.optString("healthSummary", "N/A"),
                specialist = jsonObj.optString("specialist", "Consult PCP"),
                recommendations = jsonObj.optString("recommendations", "N/A")
            )
            
            ReportResult.Success(summary)
        } catch (e: Exception) {
            ReportResult.Error(e.message ?: "Analysis failed")
        }
    }

    override suspend fun saveAnalysis(reportId: String, extractedText: String, analysisResult: String): ReportResult<Unit> {
        if (firestore == null) return ReportResult.Error("Firestore not initialized")
        
        return try {
            firestore!!.collection("reports").document(reportId)
                .update(
                    mapOf(
                        "extractedText" to extractedText,
                        "analysisResult" to analysisResult
                    )
                ).await()
            ReportResult.Success(Unit)
        } catch (e: Exception) {
            ReportResult.Error(e.message ?: "Failed to save analysis")
        }
    }
}
