package com.liveoverflow.app

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 对接 Supabase 后端，读取最新消息。
 * 通过 REST API 每 3 秒轮询 messages 表，取最新一条。
 */
class SupabaseClient(private val onMessage: (String) -> Unit) {

    companion object {
        private const val SUPABASE_URL = "https://txolefxthyybdgzvitzv.supabase.co"
        private const val SUPABASE_KEY = "sb_publishable_AzYSZ8L5oE8ZJvc0O7XNhQ_hvAP8IlQ"
        private const val TABLE = "messages"

        @Volatile
        private var lastMessageId: Long = -1
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    /**
     * 拉取最新一条消息。返回是否抓到新消息。
     */
    fun fetchLatestMessage(): Boolean {
        val url = "$SUPABASE_URL/rest/v1/$TABLE" +
                "?select=id,content" +
                "&order=id.desc" +
                "&limit=1"
        val request = Request.Builder()
            .url(url)
            .header("apikey", SUPABASE_KEY)
            .header("Authorization", "Bearer $SUPABASE_KEY")
            .get()
            .build()

        return try {
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return false
                val body = resp.body?.string() ?: return false
                if (body.isBlank() || body == "[]") return false

                val arr = JSONArray(body)
                if (arr.length() == 0) return false

                val obj = arr.getJSONObject(0)
                val id = obj.optLong("id", -1)
                val content = obj.optString("content", "")
                if (content.isBlank()) return false

                if (id != lastMessageId) {
                    lastMessageId = id
                    onMessage(content)
                    return true
                }
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
