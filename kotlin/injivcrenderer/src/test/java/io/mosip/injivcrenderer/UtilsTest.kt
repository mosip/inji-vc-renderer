package io.mosip.injivcrenderer

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response
import org.junit.Test
import org.mockito.kotlin.*
import java.io.IOException

class UtilsTest {


    @Test(expected = IOException::class)
    fun `fetchSvgAsText throws IOException when response is not successful`() {
        val mockClient = mock<OkHttpClient>()
        val mockCall = mock<Call>()
        val mockResponse = mock<Response>()
        val url = "http://example.com/test.svg"

        whenever(mockClient.newCall(any())).thenReturn(mockCall)
        whenever(mockCall.execute()).thenReturn(mockResponse)

        whenever(mockResponse.isSuccessful).thenReturn(false)

        Utils.fetchSvgAsText(url)
    }

    @Test(expected = IOException::class)
    fun `fetchSvgAsText throws IOException for incorrect Content-Type`() {

        val mockClient = mock<OkHttpClient>()
        val mockCall = mock<Call>()
        val mockResponse = mock<Response>()
        val url = "http://example.com/test.svg"

        whenever(mockClient.newCall(any())).thenReturn(mockCall)
        whenever(mockCall.execute()).thenReturn(mockResponse)

        whenever(mockResponse.isSuccessful).thenReturn(true)
        whenever(mockResponse.header("Content-Type")).thenReturn("text/html")

        Utils.fetchSvgAsText(url)
    }

    @Test(expected = IOException::class)
    fun `fetchSvgAsText throws IOException for empty response body`() {

        val mockClient = mock<OkHttpClient>()
        val mockCall = mock<Call>()
        val mockResponse = mock<Response>()
        val url = "http://example.com/test.svg"

        whenever(mockClient.newCall(any())).thenReturn(mockCall)
        whenever(mockCall.execute()).thenReturn(mockResponse)

        whenever(mockResponse.isSuccessful).thenReturn(true)
        whenever(mockResponse.header("Content-Type")).thenReturn("image/svg+xml")
        whenever(mockResponse.body).thenReturn(null) // Simulate empty response body

        Utils.fetchSvgAsText(url)
    }
}
