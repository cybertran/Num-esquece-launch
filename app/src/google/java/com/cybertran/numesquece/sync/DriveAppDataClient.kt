package com.cybertran.numesquece.sync

import android.net.Uri
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject

internal data class RemoteSnapshot(
    val payload: String?,
    val existingFileIds: List<String>,
)

internal class DriveAppDataClient {
    fun downloadLatest(accessToken: String): RemoteSnapshot {
        val listUrl = Uri.parse(DRIVE_FILES_URL).buildUpon()
            .appendQueryParameter("spaces", "appDataFolder")
            .appendQueryParameter("q", "name = '" + FILE_NAME + "' and trashed = false")
            .appendQueryParameter("orderBy", "modifiedTime desc")
            .appendQueryParameter("pageSize", "20")
            .appendQueryParameter("fields", "files(id,modifiedTime)")
            .build()
            .toString()

        val listBody = request("GET", listUrl, accessToken)
        val files = JSONObject(listBody).optJSONArray("files") ?: JSONArray()
        val ids = buildList {
            for (index in 0 until files.length()) {
                val id = files.getJSONObject(index).optString("id")
                if (id.isNotBlank()) add(id)
            }
        }

        if (ids.isEmpty()) {
            return RemoteSnapshot(payload = null, existingFileIds = emptyList())
        }

        val downloadUrl = DRIVE_FILES_URL + "/" + ids.first() + "?alt=media"
        return RemoteSnapshot(
            payload = request("GET", downloadUrl, accessToken),
            existingFileIds = ids,
        )
    }

    fun uploadSnapshot(
        accessToken: String,
        payload: String,
        existingFileIds: List<String>,
    ) {
        val boundary = "num-esquece-" + System.nanoTime()
        val metadata = JSONObject()
            .put("name", FILE_NAME)
            .put("parents", JSONArray().put("appDataFolder"))
            .toString()

        val body = buildString {
            append("--").append(boundary).append("\r\n")
            append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
            append(metadata).append("\r\n")
            append("--").append(boundary).append("\r\n")
            append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
            append(payload).append("\r\n")
            append("--").append(boundary).append("--\r\n")
        }

        val uploadUrl = Uri.parse(DRIVE_UPLOAD_URL).buildUpon()
            .appendQueryParameter("uploadType", "multipart")
            .appendQueryParameter("fields", "id")
            .build()
            .toString()

        val created = request(
            method = "POST",
            url = uploadUrl,
            accessToken = accessToken,
            contentType = "multipart/related; boundary=" + boundary,
            body = body,
        )
        val newId = JSONObject(created).getString("id")

        existingFileIds
            .filter { it != newId }
            .forEach { oldId ->
                runCatching {
                    request("DELETE", DRIVE_FILES_URL + "/" + oldId, accessToken)
                }
            }
    }

    private fun request(
        method: String,
        url: String,
        accessToken: String,
        contentType: String? = null,
        body: String? = null,
    ): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = method
            connection.connectTimeout = 15_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("Authorization", "Bearer " + accessToken)
            connection.setRequestProperty("Accept", "application/json")

            if (contentType != null) {
                connection.setRequestProperty("Content-Type", contentType)
            }
            if (body != null) {
                connection.doOutput = true
                connection.outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                    writer.write(body)
                }
            }

            val code = connection.responseCode
            val response = if (code == HttpURLConnection.HTTP_NO_CONTENT) {
                ""
            } else {
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            }

            if (code !in 200..299) {
                throw IOException(
                    "Google Drive respondeu " + code +
                        if (response.isBlank()) "." else ": " + response.take(240)
                )
            }
            response
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private const val FILE_NAME = "num-esquece-sync.json"
        private const val DRIVE_FILES_URL = "https://www.googleapis.com/drive/v3/files"
        private const val DRIVE_UPLOAD_URL = "https://www.googleapis.com/upload/drive/v3/files"
    }
}
