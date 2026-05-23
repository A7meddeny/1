package com.masar.portal.network

import com.masar.portal.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * عميل HTTP بسيط بدون مكتبات خارجية ثقيلة.
 * يتعامل مع API الموجود في موقع مسار.
 */
class MasarApi(private val baseUrl: String) {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    /** فحص أن الرابط يستجيب */
    suspend fun ping(): Boolean = withContext(Dispatchers.IO) {
        try {
            val conn = (URL("$baseUrl/portal.php").openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
            }
            val code = conn.responseCode
            conn.disconnect()
            code in 200..399
        } catch (e: Exception) { false }
    }

    /** جلب إعدادات العلامة (الشعار، الاسم) */
    suspend fun fetchBrand(): BrandInfo? = withContext(Dispatchers.IO) {
        runCatching {
            val res = httpGet("$baseUrl/api/portal.php?action=brand")
            json.decodeFromString<BrandResponse>(res).brand
        }.getOrNull()
    }

    /** تسجيل دخول المندوب */
    suspend fun login(nid: String, password: String): LoginResponse = withContext(Dispatchers.IO) {
        try {
            val body = """{"national_id":${esc(nid)},"password":${esc(password)}}"""
            val res = httpPost("$baseUrl/api/portal.php?action=app_login", body)
            json.decodeFromString<LoginResponse>(res)
        } catch (e: Exception) {
            LoginResponse(ok = false, error = "تعذّر الاتصال: ${e.message ?: ""}")
        }
    }

    /** جلب كل بيانات المندوب */
    suspend fun fetchMe(nid: String, token: String): MeResponse = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/api/portal.php?action=app_me&nid=${enc(nid)}&token=${enc(token)}"
            val res = httpGet(url)
            json.decodeFromString<MeResponse>(res)
        } catch (e: Exception) {
            MeResponse(ok = false, error = "تعذّر الاتصال: ${e.message ?: ""}")
        }
    }

    /** إرسال طلب جديد (شكوى/سلفة/إجازة/حادث/بنزين) — مع التوكن */
    suspend fun submitRequest(
        nid: String,
        token: String,
        type: String,
        details: String,
        amount: Double? = null,
        odometer: Int? = null,
    ): SimpleResponse = withContext(Dispatchers.IO) {
        try {
            val sb = StringBuilder("{")
            sb.append("\"national_id\":${esc(nid)},")
            sb.append("\"token\":${esc(token)},")
            sb.append("\"type\":${esc(type)},")
            sb.append("\"details\":${esc(details)}")
            if (amount != null)   sb.append(",\"amount\":$amount")
            if (odometer != null) sb.append(",\"odometer\":$odometer")
            sb.append("}")
            val res = httpPost("$baseUrl/api/portal.php?action=app_submit", sb.toString())
            json.decodeFromString<SimpleResponse>(res)
        } catch (e: Exception) {
            SimpleResponse(ok = false, error = "تعذّر الإرسال: ${e.message ?: ""}")
        }
    }

    // ============ Helpers ============

    private fun httpGet(url: String): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 15000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
        }
        return conn.inputStream.bufferedReader().use { it.readText() }.also { conn.disconnect() }
    }

    private fun httpPost(url: String, body: String): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 15000
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
        }
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
        val text = try {
            conn.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            // اقرأ الخطأ من ErrorStream لاستخراج JSON
            conn.errorStream?.bufferedReader()?.use { it.readText() } ?: throw e
        }
        conn.disconnect()
        return text
    }

    private fun esc(v: String): String = json.encodeToString(kotlinx.serialization.builtins.serializer(), v)
    private fun enc(v: String): String = URLEncoder.encode(v, "UTF-8")
}
