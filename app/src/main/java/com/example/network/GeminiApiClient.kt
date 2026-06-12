package com.example.network

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    data class WatermarkDetectionResult(
        val ymin: Float,
        val xmin: Float,
        val ymax: Float,
        val xmax: Float,
        val label: String
    )

    /**
     * Sends the image to Gemini 3.5 Flash to automatically detect watermarks or text overlays.
     * Scale result is normalized from 0 to 1000.
     */
    suspend fun detectWatermark(bitmap: Bitmap): WatermarkDetectionResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("API Key is missing. Please configure GEMINI_API_KEY in your Secrets panel.")
        }

        // Resizing for speed and cost efficiency
        val maxDim = 800
        val resizedBitmap = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val (w, h) = if (ratio > 1f) {
                Pair(maxDim, (maxDim / ratio).toInt())
            } else {
                Pair((maxDim * ratio).toInt(), maxDim)
            }
            Bitmap.createScaledBitmap(bitmap, w, h, true)
        } else {
            bitmap
        }

        // Convert to Base64
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

        val systemPrompt = """
            You are a specialized AI designed to locate watermarks, text overlays, digital stamps, logos, or identifiers on images.
            Determine the exact bounding box enclosing the MAIN watermark, logo or text label overlaid on the image.
            Return the bounding box as a JSON object of normalized coordinates scaled from 0 to 1000.
            
            Format:
            {
              "ymin": number,
              "xmin": number,
              "ymax": number,
              "xmax": number,
              "label": "Brief description of the text or logo found"
            }
            
            Do not output any additional text, markdown backticks, or other information. Return only the raw JSON.
        """.trimIndent()

        // JSON Request Payload
        val requestJson = JSONObject().apply {
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Find the coordinates of the watermark or text logo in this image.")
                        })
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.1)
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", org.json.JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            })
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val url = "$BASE_URL?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Request failed: Code=${response.code}, Msg=$errBody")
                    throw Exception("API call failed with code ${response.code}")
                }

                val responseBody = response.body?.string() ?: throw Exception("Empty response body")
                Log.d(TAG, "Response: $responseBody")

                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.getJSONArray("candidates")
                val textResponse = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                val cleanJson = textResponse.trim().removeSurrounding("```json", "```").trim()
                val resultObj = JSONObject(cleanJson)

                WatermarkDetectionResult(
                    ymin = resultObj.optDouble("ymin", 800.0).toFloat(),
                    xmin = resultObj.optDouble("xmin", 600.0).toFloat(),
                    ymax = resultObj.optDouble("ymax", 950.0).toFloat(),
                    xmax = resultObj.optDouble("xmax", 950.0).toFloat(),
                    label = resultObj.optString("label", "علامة مائية مكتشفة")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during detectWatermark", e)
            throw e
        }
    }
}
