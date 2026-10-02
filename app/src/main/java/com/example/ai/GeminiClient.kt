package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

/**
 * AI entry point. Order of preference:
 *  1. The `askGemini` Cloud Function (functions/index.js) — the Gemini key lives only on the
 *     server, so nothing secret ships in the APK. Used whenever Firebase is configured and
 *     the user is signed in.
 *  2. Direct REST call with BuildConfig.GEMINI_API_KEY — DEBUG builds only, for local
 *     development. Release builds never read the embedded key.
 *  3. A clearly labelled offline fallback message (never presented as an AI answer).
 */
object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val MODELS = listOf(
        "gemini-2.5-flash",
        "gemini-2.0-flash"
    )

    const val OFFLINE_NOTICE =
        "⚠️ AI xizmati hozir mavjud emas — quyida umumiy (oldindan tayyorlangan) ma'lumot berilgan, bu shaxsiy tibbiy tahlil EMAS.\n\n"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            // Request/response bodies contain users' health data; never log them on release.
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
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

    private fun serverProxyAvailable(): Boolean = try {
        // getInstance() throws when no Firebase project is configured -> treated as unavailable.
        FirebaseAuth.getInstance().currentUser != null
    } catch (t: Throwable) {
        false
    }

    private fun directKeyAvailable(): Boolean =
        BuildConfig.DEBUG && BuildConfig.GEMINI_API_KEY.isNotEmpty() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

    suspend fun generateText(prompt: String, systemInstruction: String? = null): String =
        generate(prompt, systemInstruction, null, null)

    suspend fun generateMultimodal(prompt: String, base64Image: String, mimeType: String, systemInstruction: String? = null): String =
        generate(prompt, systemInstruction, base64Image, mimeType)

    private suspend fun generate(prompt: String, systemInstruction: String?, base64Image: String?, mimeType: String?): String {
        if (serverProxyAvailable()) {
            callProxy(prompt, systemInstruction, base64Image, mimeType)?.let { return it }
        }
        if (directKeyAvailable()) {
            callDirect(prompt, systemInstruction, base64Image, mimeType)?.let { return it }
        } else if (!serverProxyAvailable()) {
            Log.w(TAG, "No AI backend configured (no Firebase session and no debug key).")
        }
        return OFFLINE_NOTICE + generateLocalFallbackResponse(prompt)
    }

    private suspend fun callProxy(prompt: String, systemInstruction: String?, base64Image: String?, mimeType: String?): String? =
        suspendCancellableCoroutine { cont ->
            try {
                val payload = hashMapOf<String, Any?>(
                    "prompt" to prompt,
                    "systemInstruction" to systemInstruction,
                    "imageBase64" to base64Image,
                    "mimeType" to mimeType
                )
                FirebaseFunctions.getInstance()
                    .getHttpsCallable("askGemini")
                    .call(payload)
                    .addOnSuccessListener { result ->
                        val text = (result.data as? Map<*, *>)?.get("text") as? String
                        if (cont.isActive) cont.resume(text?.takeIf { it.isNotBlank() })
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "askGemini proxy failed: ${e.localizedMessage}")
                        if (cont.isActive) cont.resume(null)
                    }
            } catch (t: Throwable) {
                Log.w(TAG, "askGemini proxy unavailable: ${t.localizedMessage}")
                if (cont.isActive) cont.resume(null)
            }
        }

    private suspend fun callDirect(prompt: String, systemInstruction: String?, base64Image: String?, mimeType: String?): String? {
        val parts = mutableListOf(Part(text = prompt))
        if (base64Image != null && mimeType != null) {
            parts += Part(inlineData = InlineData(mimeType = mimeType, data = base64Image))
        }
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = parts)),
            systemInstruction = systemInstruction?.let { Content(parts = listOf(Part(text = it))) }
        )
        for (model in MODELS) {
            try {
                val response = apiService.generateContent(model, BuildConfig.GEMINI_API_KEY, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) return text
            } catch (e: Exception) {
                Log.w(TAG, "Gemini model '$model' failed: ${e.localizedMessage}")
            }
        }
        return null
    }

    private fun generateLocalFallbackResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("bosh") || lower.contains("og'riq") || lower.contains("isitma") || lower.contains("symptom") -> {
                """
                🩺 **Umumiy tibbiy maslahat**:

                - Ko'proq toza suv iching va yetarlicha dam oling.
                - Harorat va qon bosimini muntazam o'lchab turing.
                - Alomatlar 2-3 kundan ortiq davom etsa, **terapevt** ko'rigiga boring.

                ⚠️ Agar kuchli og'riq, hushdan ketish, nafas qisilishi yoki ko'krak qafasida og'riq bo'lsa, zudlik bilan **103** (Tez yordam) ga qo'ng'iroq qiling.
                """.trimIndent()
            }
            lower.contains("dori") || lower.contains("preparat") || lower.contains("doza") -> {
                """
                💊 **Dori vositalari haqida**:

                - Dorini faqat shifokor ko'rsatmasi bo'yicha qabul qiling.
                - Dozani o'zboshimchalik bilan o'zgartirmang, yo'riqnomani o'qing.
                - Nojo'ya ta'sir sezsangiz, qabul qilishni to'xtatib, shifokor bilan maslahatlashing.
                """.trimIndent()
            }
            lower.contains("ovqat") || lower.contains("dieta") || lower.contains("kaloriya") -> {
                """
                🥗 **Sog'lom ovqatlanish**:

                - Sabzavot, meva va oqsilga boy mahsulotlarni ko'paytiring.
                - Gazlangan ichimlik va juda yog'li taomlarni kamaytiring.
                - Kuniga kamida 1,5-2 litr suv iching.
                """.trimIndent()
            }
            else -> {
                """
                👨‍⚕️ **Umumiy maslahat**:

                Sog'liqni asrash uchun to'g'ri ovqatlanish, jismoniy faollik va yetarli uyqu muhim. Noxush alomatlar bo'lsa, shifokor ko'rigidan o'ting.
                """.trimIndent()
            }
        }
    }
}
