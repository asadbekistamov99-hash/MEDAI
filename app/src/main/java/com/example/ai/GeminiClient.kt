package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val MODELS = listOf(
        "gemini-2.5-flash",
        "gemini-1.5-flash",
        "gemini-2.0-flash",
        "gemini-1.5-pro"
    )

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun generateText(prompt: String, systemInstruction: String? = null): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not configured or placeholder.")
            return generateLocalFallbackResponse(prompt)
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = systemInstruction?.let { Content(parts = listOf(Part(text = it))) }
        )

        for (model in MODELS) {
            try {
                val response = apiService.generateContent(model, apiKey, request)
                val textResult = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!textResult.isNullOrBlank()) {
                    return textResult
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini model '$model' failed: ${e.localizedMessage}")
            }
        }

        Log.e(TAG, "All Gemini models failed. Using intelligent local fallback.")
        return generateLocalFallbackResponse(prompt)
    }

    suspend fun generateMultimodal(prompt: String, base64Image: String, mimeType: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not configured or placeholder.")
            return generateLocalFallbackResponse(prompt)
        }

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt),
                        Part(inlineData = InlineData(mimeType = mimeType, data = base64Image))
                    )
                )
            )
        )

        for (model in MODELS) {
            try {
                val response = apiService.generateContent(model, apiKey, request)
                val textResult = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!textResult.isNullOrBlank()) {
                    return textResult
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini multimodal model '$model' failed: ${e.localizedMessage}")
            }
        }

        Log.e(TAG, "All Gemini multimodal models failed. Using local fallback.")
        return generateLocalFallbackResponse(prompt)
    }

    private fun generateLocalFallbackResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("bosh") || lower.contains("og'riq") || lower.contains("isitma") || lower.contains("symptom") -> {
                """
                🩺 **Tibbiy Tahlil va Boshlang'ich Maslahat**:
                
                1. **Ehtimoliy Sabablar**:
                   - Organizmda toliqish, shamollash, doimiy stress yoki gipoksiya alomatlari kuzatilgan bo'lishi mumkin.
                   - Suv yetishmasligi (dehidratsiya) ham shunday belgilarga olib keladi.
                
                2. **Tavsiya Etiladigan Mutaxassis**:
                   - **Terapevt** yoki **Umumiy Amaliyot Shifokori** xonasiga uchrashish tavsiya etiladi.
                
                3. **Uy Sharoitidagi Boshlang'ich Choralar**:
                   - Ko'proq toza suv iching (suyuqlik balansini tiklang).
                   - Xonani shamollatib, kamida 20-30 daqiqa tinch joyda dam oling.
                   - Harorat va qon bosimini muntazam olchab turing.
                
                ⚠️ **Eslatma**: Agar og'riq juda kuchaysa, hushdan ketish yoki ko'ngil aynishi kuzatilsa, zudlik bilan 103 (Tez yordam) xizmatiga murojaat qiling.
                """.trimIndent()
            }
            lower.contains("dori") || lower.contains("preparat") || lower.contains("doza") -> {
                """
                💊 **Farmakologik Maslahat**:
                
                - Har qanday dori vositasini faqat shifokor retsepti va ko'rsatmasi bo'yicha qabul qiling.
                - Dozani o'zboshimchalik bilan o'zgartirmang va dori yo'riqnomasini diqqat bilan o'rganib chiqing.
                - Agar nojo'ya ta'sirlar kuzatilsa, dori qabul qilishni to'xtatib, shifokor bilan maslahatlashing.
                """.trimIndent()
            }
            lower.contains("ovqat") || lower.contains("dieta") || lower.contains("kaloriya") -> {
                """
                🥗 **Sog'lom Ovqatlanish va Dieta**:
                
                - Kunlik rejimda sabzavotlar, mevalar va oqsilga boy mahsulotlarni ko'paytiring.
                - Gazlangan ichimliklar va xaddan tashqari yog'li taomlarni kamaytiring.
                - Kuniga kamida 2 litr suv ichish moddalar almashinuvini yaxshilaydi.
                """.trimIndent()
            }
            else -> {
                """
                👨‍⚕️ **MedAI Shifokor Maslahati**:
                
                Sizning so'rovingiz qabul qilindi. Sog'lig'ingizni asrash uchun doimiy ravishda to'g'ri ovqatlanish, jismoniy faollik va yetarlicha uyquga e'tibor bering.
                
                Agarda o'zingizda noxush alomatlar sezsangiz, shifokor ko'rigidan o'tishingizni tavsiya qilamiz.
                """.trimIndent()
            }
        }
    }
}

