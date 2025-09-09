package io.mosip.injivcrenderer.networkManager

import io.mosip.injivcrenderer.constants.NetworkConstants.CONTENT_TYPE
import io.mosip.injivcrenderer.constants.NetworkConstants.CONTENT_TYPE_SVG
import io.mosip.injivcrenderer.exceptions.VcRendererExceptions
import okhttp3.OkHttpClient
import okhttp3.Request
class NetworkManager(
    private val traceabilityId: String,
    private val client: OkHttpClient = OkHttpClient() // default production client
) {
    fun fetchSvgAsText(url: String): String {
        val request = Request.Builder().url(url).build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw VcRendererExceptions.SvgFetchException(
                        traceabilityId,
                        this::class.simpleName,
                        "Unexpected response code $response"
                    )
                }

                val contentType = response.header("Content-Type")
                if (contentType != "image/svg+xml") {
                    throw VcRendererExceptions.SvgFetchException(
                        traceabilityId,
                        this::class.simpleName,
                        "Expected image/svg+xml but received $contentType"
                    )
                }

                response.body?.string()
                    ?: throw VcRendererExceptions.SvgFetchException(
                        traceabilityId,
                        this::class.simpleName,
                        "Empty response body"
                    )
            }
        } catch (e: VcRendererExceptions.SvgFetchException) {
            throw e
        } catch (e: Exception) {
            throw VcRendererExceptions.SvgFetchException(
                traceabilityId,
                this::class.simpleName,
                e.message ?: "Unexpected error"
            )
        }
    }
}
