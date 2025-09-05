// Fixed GroqWhisperApi.kt
package com.example.speech_textime.net

import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit

data class WhisperResponse(val text: String)

interface GroqWhisperApi {

    @Multipart
    @POST("audio/transcriptions")
    suspend fun transcribe(
        @Part file: okhttp3.MultipartBody.Part,
        @Part("model") model: RequestBody
    ): Response<WhisperResponse>

    companion object {
        fun create(apiKey: String): GroqWhisperApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY // Changed to BODY for better debugging
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val req = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $apiKey")
                        .build()
                    chain.proceed(req)
                }
                .build()

            return Retrofit.Builder()
                .baseUrl("https://api.groq.com/openai/v1/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GroqWhisperApi::class.java)
        }
    }
}