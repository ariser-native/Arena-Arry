package com.example.service

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getTacticalAdvice(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            Log.w(TAG, "API Key is placeholder or empty. Using simulated offline strategy.")
            return@withContext getOfflineMockAdvice(prompt)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        
        val requestJson = """
            {
                "contents": [
                    {
                        "parts": [
                            {
                                "text": ${escapeJsonString(prompt)}
                            }
                        ]
                    }
                ],
                "systemInstruction": {
                    "parts": [
                        {
                            "text": "You are Arry AI, a pro Free Fire and battle royale esports tactical coach. Speak in a mix of Hindi, Urdu and English (Hinglish/Roman Urdu), matching the language style of elite South Asian gamers. Give short, punchy, tactical, motivational, or funny advice about loadouts, squad coordination, matchmaking, rush gameplay, and circle rotations. Limit your response to 2-3 brief, highly actionable bullet points or a short paragraph."
                        }
                    ]
                }
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(requestJson.toRequestBody(mediaType))
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Gemini API error: ${response.code} $errBody")
                    return@withContext "Error: Could not sync with game server. Showing offline tactics:\n\n${getOfflineMockAdvice(prompt)}"
                }

                val bodyString = response.body?.string() ?: return@withContext "Server returned empty response."
                
                val responseMap = moshi.adapter(Map::class.java).fromJson(bodyString)
                val candidates = responseMap?.get("candidates") as? List<*>
                val firstCandidate = candidates?.firstOrNull() as? Map<*, *>
                val content = firstCandidate?.get("content") as? Map<*, *>
                val parts = content?.get("parts") as? List<*>
                val firstPart = parts?.firstOrNull() as? Map<*, *>
                val text = firstPart?.get("text") as? String
                
                text ?: "Tactical transmission received, but format was scrambled. Rush the circle!"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling Gemini API", e)
            "Error: ${e.message}\n\n${getOfflineMockAdvice(prompt)}"
        }
    }

    private fun escapeJsonString(s: String): String {
        val builder = java.lang.StringBuilder()
        builder.append("\"")
        for (i in 0 until s.length) {
            val c = s[i]
            when (c) {
                '\"' -> builder.append("\\\"")
                '\\' -> builder.append("\\\\")
                '\n' -> builder.append("\\n")
                '\r' -> builder.append("\\r")
                '\t' -> builder.append("\\t")
                else -> builder.append(c)
            }
        }
        builder.append("\"")
        return builder.toString()
    }

    private fun getOfflineMockAdvice(prompt: String): String {
        val lowercasePrompt = prompt.lowercase()
        return when {
            lowercasePrompt.contains("weapon") || lowercasePrompt.contains("gun") || lowercasePrompt.contains("m4a1") || lowercasePrompt.contains("awm") || lowercasePrompt.contains("loadout") -> {
                "🔥 *Arry's Weapon Tip:* Free Fire main M1887 shotgun close-range rush ke liye aur AWM long range sniping ke liye best hai. Woodpecker use karo agar single-tap headshot chahye!"
            }
            lowercasePrompt.contains("rush") || lowercasePrompt.contains("attack") -> {
                "🏃 *Arry's Tactical Rush:* Gloo wall lagate hue aage barho. Grenade cook karke fenko taake enemy cover se bahar aaye. Alok ya Chrono ki skill sahi time pe activate karna mat bhoolna!"
            }
            lowercasePrompt.contains("gloo") || lowercasePrompt.contains("shield") || lowercasePrompt.contains("cover") -> {
                "🛡️ *Arry's Gloo Wall Trick:* 360-degree gloo wall lagane ki practice karo training ground mein. Direct open field mein loot mat karo, hamesha cover le ke spray karo!"
            }
            else -> {
                "🎮 *Arry's Pro Tip:* Zone control bohot zaroori hai. Safe zone k corners pe raho aur rotate karte raho. Rush gameplay tabhi karo jab squad ready ho!"
            }
        }
    }
}
