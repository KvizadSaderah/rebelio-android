package com.kaidendev.rebelioclientandroid.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.kaidendev.rebelioclientandroid.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class UpdateInfo(
    val version: String,
    val downloadUrl: String,
    val releaseNotes: String
)

class UpdateChecker {
    private val client = OkHttpClient()

    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.github.com/repos/KvizadSaderah/rebelio-android/releases/latest")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null

                val jsonStr = response.body?.string() ?: return@withContext null
                val json = JSONObject(jsonStr)
                
                val tagName = json.getString("tag_name")
                val latestVersion = tagName.removePrefix("v")
                val currentVersion = BuildConfig.VERSION_NAME

                if (compareVersions(latestVersion, currentVersion) > 0) {
                    val assets = json.getJSONArray("assets")
                    if (assets.length() > 0) {
                        val downloadUrl = assets.getJSONObject(0).getString("browser_download_url")
                        val body = json.optString("body", "New version available")
                        return@withContext UpdateInfo(latestVersion, downloadUrl, body)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        val length = maxOf(parts1.size, parts2.size)

        for (i in 0 until length) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1 - p2
        }
        return 0
    }

    fun downloadUpdate(context: Context, url: String, version: String) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Rebelio Update")
            .setDescription("Downloading version $version")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "rebelio-$version.apk")
            .setMimeType("application/vnd.android.package-archive")

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }
}
