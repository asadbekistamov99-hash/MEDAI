package com.example.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Client for the AI features (symptom analysis, drug info, lab/prescription vision, chat).
 *
 * ## Why this no longer calls the Gemini REST API directly
 *
 * It used to build its own Retrofit client and pass `BuildConfig.GEMINI_API_KEY` as a `?key=`
 * query parameter. That put the key inside the APK — recoverable by anyone who unzips the app —
 * and routed every prompt (symptoms, lab reports, prescriptions) straight to Google with no
 * control over logging, quotas or abuse. The key in a URL also lands in intermediate proxy logs.
 *
 * The call now goes through the `generateContent` Cloud Function (see functions/index.js), which
 * holds the key, enforces a per-user daily rate limit, and is the single auditable hop. This is
 * what metadata.json already advertised as MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API.
 *
 * ## On-device key (optional)
 *
 * With no Firebase project the callable cannot work. If `GEMINI_API_KEY` is set in `.env` at the repo
 * root (git-ignored; see .env.example) the app calls Gemini directly from the device instead.
 *
 * ## Fallback behaviour
 *
 * If the server is not deployed, not configured, or the user is offline, [generateLocalFallbackResponse]
 * answers instead. That is a deliberate trade-off: the app stays usable, but the user is getting
 * a canned response, not medical analysis. Callers that care should surface that to the user
 * rather than presenting the text as a real answer — see [Result.source].
 */
object GeminiClient {

    private const val TAG = "GeminiClient"
    private const val CALLABLE = "generateContent"

    /** Where a given answer came from, so the UI can be honest about it. */
    enum class Source { AI, LOCAL_FALLBACK }

    data class Result(val text: String, val source: Source)

    /**
     * Generates a text completion.
     *
     * @param systemInstruction optional persona/language/system prompt for the model.
     */
    suspend fun generate(prompt: String, systemInstruction: String? = null): Result {
        val response = callServer(prompt, systemInstruction)
            ?: callDirect(prompt, systemInstruction)
        return if (response.isNullOrBlank()) {
            Result(generateLocalFallbackResponse(prompt), Source.LOCAL_FALLBACK)
        } else {
            Result(response, Source.AI)
        }
    }

    /**
     * Analyses an image (lab report, prescription, pill) together with a prompt.
     *
     * The image is uploaded as a base64 data URI to the same callable; the function relays it to
     * the multimodal Gemini endpoint. Nothing about the image is persisted server-side.
     */
    suspend fun generateMultimodal(prompt: String, base64Image: String, mimeType: String): Result {
        val response = callServer(
            prompt = prompt,
            systemInstruction = null,
            inlineData = base64Image,
            mimeType = mimeType
        ) ?: callDirect(prompt, null, base64Image, mimeType)
        return if (response.isNullOrBlank()) {
            Result(generateLocalFallbackResponse(prompt), Source.LOCAL_FALLBACK)
        } else {
            Result(response, Source.AI)
        }
    }

    /**
     * Bridges the Task-based Firebase Functions SDK into a coroutine.
     *
     * Returns null on any failure — an undeployed function, a missing key, a rate-limit refusal
     * or a network error are all treated the same way, so the caller falls back rather than
     * surfacing a Firebase exception into the UI. The user-visible difference between "AI" and
     * "canned text" is carried in [Result.source] instead.
     */
    private suspend fun callServer(
        prompt: String,
        systemInstruction: String?,
        inlineData: String? = null,
        mimeType: String? = null
    ): String? = suspendCancellableCoroutine { continuation ->
        // Without a real google-services.json the app runs on a placeholder Firebase project; the
        // callable can never succeed there and would only stall until its timeout.
        if (!isFirebaseConfigured()) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        try {
            val data = hashMapOf<String, Any?>("prompt" to prompt)
            if (systemInstruction != null) {
                data["systemInstruction"] = systemInstruction
            }
            if (inlineData != null && mimeType != null) {
                data["inlineData"] = inlineData
                data["mimeType"] = mimeType
            }

            FirebaseFunctions.getInstance()
                .getHttpsCallable(CALLABLE)
                .call(data)
                .addOnSuccessListener { result ->
                    val text = result.data as? Map<*, *>
                    val body = text?.get("text") as? String
                    if (continuation.isActive) continuation.resume(body)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "AI proxy call failed: ${e.message}")
                    if (continuation.isActive) continuation.resume(null)
                }
        } catch (e: Exception) {
            Log.w(TAG, "AI proxy unavailable: ${e.message}")
            if (continuation.isActive) continuation.resume(null)
        }
    }


    private fun isFirebaseConfigured(): Boolean = try {
        val key = com.google.firebase.FirebaseApp.getInstance().options.apiKey
        !key.contains("Fake", ignoreCase = true)
    } catch (e: Exception) {
        false
    }

    /** True when a real key was provided through `.env` (GEMINI_API_KEY=...); the template placeholder does not count. */
    private fun directKey(): String? =
        BuildConfig.GEMINI_API_KEY.trim().takeIf { it.isNotEmpty() && !it.startsWith("MY_") }

    private val DIRECT_MODELS = listOf("gemini-2.5-flash", "gemini-2.0-flash", "gemini-1.5-flash")

    /**
     * On-device call used when the Cloud Function is unavailable (it needs a Firebase project and
     * a signed-in Firebase user, which this app does not have). The key is sent in a header, never
     * in the URL. Note that a key shipped inside an APK can be extracted: use it for your own
     * builds, not for a public release.
     */
    private suspend fun callDirect(
        prompt: String,
        systemInstruction: String?,
        inlineData: String? = null,
        mimeType: String? = null
    ): String? {
        val key = directKey() ?: return null
        return withContext(Dispatchers.IO) {
            val parts = JSONArray().put(JSONObject().put("text", prompt))
            if (inlineData != null && mimeType != null) {
                parts.put(JSONObject().put("inline_data", JSONObject().put("mime_type", mimeType).put("data", inlineData)))
            }
            val body = JSONObject().put("contents", JSONArray().put(JSONObject().put("role", "user").put("parts", parts)))
            if (systemInstruction != null) {
                body.put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstruction))))
            }
            val payload = body.toString().toByteArray()

            for (model in DIRECT_MODELS) {
                var conn: HttpURLConnection? = null
                try {
                    conn = (URL("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent").openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        connectTimeout = 15_000
                        readTimeout = 60_000
                        doOutput = true
                        setRequestProperty("Content-Type", "application/json")
                        setRequestProperty("x-goog-api-key", key)
                    }
                    conn.outputStream.use { it.write(payload) }
                    if (conn.responseCode !in 200..299) {
                        Log.w(TAG, "Gemini $model returned HTTP ${conn.responseCode}")
                        continue
                    }
                    val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                    val text = json.optJSONArray("candidates")?.optJSONObject(0)
                        ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
                    if (!text.isNullOrBlank()) return@withContext text
                } catch (e: Exception) {
                    Log.w(TAG, "Gemini $model failed: ${e.message}")
                } finally {
                    conn?.disconnect()
                }
            }
            null
        }
    }

    /**
     * Canned, non-diagnostic responses used when the AI service cannot be reached.
     *
     * These are intentionally conservative general wellness information. They are NOT a
     * diagnosis and must not be presented as one — a user who cannot reach the server should be
     * told the service is degraded, not handed a confident-looking answer.
     */
    private fun generateLocalFallbackResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("bosh") || lower.contains("og'riq") || lower.contains("isitma") ||
                lower.contains("symptom") -> {
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

                ℹ️ *AI tahlil xizmati vaqtincha ishlamayapti — yuqoridagi umumiy ma'lumot berildi. Aniq tashxis qo'yish uchun shifokorga murojaat qiling.*
                """.trimIndent()
            }
            lower.contains("dori") || lower.contains("preparat") || lower.contains("doza") -> {
                """
                💊 **Farmakologik Maslahat**:

                - Har qanday dori vositasini faqat shifokor retsepti va ko'rsatmasi bo'yicha qabul qiling.
                - Dozani o'zbstimchalik bilan o'zgartirmang va dori yo'riqnomasini diqqat bilan o'rganib chiqing.
                - Agar nojo'ya ta'sirlar kuzatilsa, dori qabul qilishni to'xtatib, shifokor bilan maslahatlashing.

                ℹ️ *AI xizmati vaqtincha ishlamayapti — umumiy ma'lumot berildi.*
                """.trimIndent()
            }
            lower.contains("ovqat") || lower.contains("dieta") || lower.contains("kaloriya") -> {
                """
                🥗 **Sog'lom Ovqatlanish va Dieta**:

                - Kunlik rejimda sabzavotlar, mevalar va oqsilga boy mahsulotlarni ko'paytiring.
                - Gazlangan ichimliklar va haddan tashqari yog'li taomlarni kamaytiring.
                - Kuniga kamida 2 litr suv ichish moddalar almashinuvini yaxshilaydi.

                ℹ️ *AI xizmati vaqtincha ishlamayapti — umumiy ma'lumot berildi.*
                """.trimIndent()
            }
            else -> {
                """
                👨‍⚕️ **MedAI Shifokor Maslahati**:

                Sizning so'rovingiz qabul qilindi. Sog'lig'ingizni asrash uchun doimiy ravishda to'g'ri ovqatlanish, jismoniy faollik va yetarlicha uyquga e'tibor bering.

                Agarda o'zingizda noxush alomatlar sezsangiz, shifokor ko'rigidan o'tishingizni tavsiya qilamiz.

                ℹ️ *AI xizmati vaqtincha ishlamayapti — umumiy ma'lumot berildi.*
                """.trimIndent()
            }
        }
    }
}
