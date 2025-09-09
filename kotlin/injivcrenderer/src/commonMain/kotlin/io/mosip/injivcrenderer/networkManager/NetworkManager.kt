package io.mosip.injivcrenderer.networkManager

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class NetworkManager {

    fun fetchSvgAsText(url: String): String {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected response code $response")
                }
                val contentType = response.header(CONTENT_TYPE)
                if (contentType != CONTENT_TYPE_SVG) {
                    throw IOException("Expected image/svg+xml but received $contentType")
                }
                response.body?.string() ?: throw IOException("Empty response body")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Unexpected error", e)
        }
    }

    companion object {
        const val CONTENT_TYPE_SVG = "image/svg+xml"
        const val CONTENT_TYPE = "Content-Type"
    }
}