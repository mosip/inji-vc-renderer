package io.mosip.injivcrenderer

import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException

object Utils {
    fun fetchSvgAsText(url: String): String {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }
            val contentType = response.header("Content-Type")
            if (contentType != "image/svg+xml") {
                throw IOException("Expected image/svg+xml but received $contentType")
            }
            return response.body?.string() ?: throw IOException("Empty response body")
        }
    }

}