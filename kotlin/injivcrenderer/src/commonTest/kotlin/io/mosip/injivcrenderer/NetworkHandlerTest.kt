package io.mosip.injivcrenderer

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class NetworkHandlerTest {
    @Test(expected = IOException::class)
    fun `fetchSvgAsText throws IOException when response is not successful`() {
        val mockClient = mock<OkHttpClient>()
        val mockCall = mock<Call>()
        val mockResponse = mock<Response>()
        val url = "http://example.com/test.svg"

        whenever(mockClient.newCall(any())).thenReturn(mockCall)
        whenever(mockCall.execute()).thenReturn(mockResponse)

        whenever(mockResponse.isSuccessful).thenReturn(false)

        NetworkHandler().fetchSvgAsText(url)
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

        NetworkHandler().fetchSvgAsText(url)
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

        NetworkHandler().fetchSvgAsText(url)
    }

}