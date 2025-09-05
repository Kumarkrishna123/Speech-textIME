// Fixed WhisperRepo.kt
package com.example.speech_textime.net

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class WhisperRepo(private val api: GroqWhisperApi) {

    suspend fun transcribeFile(audio: File): String = withContext(Dispatchers.IO) {
        Log.d("WhisperRepo", "Starting transcription for file: ${audio.name}, size: ${audio.length()} bytes")

        if (!audio.exists() || audio.length() == 0L) {
            throw IllegalStateException("Audio file is empty or doesn't exist")
        }

        val filePart = MultipartBody.Part.createFormData(
            name = "file",
            filename = audio.name,
            body = audio.asRequestBody("audio/m4a".toMediaType())
        )

        val modelPart = "whisper-large-v3".toRequestBody("text/plain".toMediaType())

        Log.d("WhisperRepo", "Sending request to Groq API...")

        try {
            val response = api.transcribe(filePart, modelPart)

            Log.d("WhisperRepo", "Response code: ${response.code()}")

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e("WhisperRepo", "API Error: ${response.code()} - $errorBody")
                throw IllegalStateException("Transcribe failed: ${response.code()} - $errorBody")
            }

            val result = response.body()?.text?.trim() ?: ""
            Log.d("WhisperRepo", "Transcription result: '$result'")

            if (result.isEmpty()) {
                Log.w("WhisperRepo", "Empty transcription result - possible silent audio")
                return@withContext "No speech detected"
            }

            return@withContext result

        } catch (e: Exception) {
            Log.e("WhisperRepo", "Transcription failed", e)
            throw e
        }
    }
}