package io.mosip.injivcrenderer.networkManager

import io.mosip.injivcrenderer.constants.VcRendererErrorCodes.SVG_FETCH_ERROR
import io.mosip.injivcrenderer.exceptions.VcRendererExceptions
import okhttp3.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
class NetworkManagerTest {

    private val traceId = "test-trace-id"
    private val testUrl = "http://example.com/test.svg"

    @Test
    fun `throws SvgFetchException when response is not successful`() {
        val mockClient = mock<OkHttpClient>()
        val mockCall = mock<Call>()
        val mockResponse = mock<Response>()

        whenever(mockClient.newCall(any())).thenReturn(mockCall)
        whenever(mockCall.execute()).thenReturn(mockResponse)
        whenever(mockResponse.isSuccessful).thenReturn(false)

        val manager = NetworkManager(traceId, mockClient)

        val ex = assertFailsWith<VcRendererExceptions.SvgFetchException> {
            manager.fetchSvgAsText(testUrl)
        }

        assertEquals(SVG_FETCH_ERROR, ex.errorCode)
        assert(ex.message!!.contains("Unexpected response code"))
    }

    @Test
    fun `throws SvgFetchException for incorrect Content-Type`() {
        val mockClient = mock<OkHttpClient>()
        val mockCall = mock<Call>()
        val mockResponse = mock<Response>()

        whenever(mockClient.newCall(any())).thenReturn(mockCall)
        whenever(mockCall.execute()).thenReturn(mockResponse)
        whenever(mockResponse.isSuccessful).thenReturn(true)
        whenever(mockResponse.header("Content-Type")).thenReturn("text/html")

        val manager = NetworkManager(traceId, mockClient)

        val ex = assertFailsWith<VcRendererExceptions.SvgFetchException> {
            manager.fetchSvgAsText(testUrl)
        }
        assertEquals(SVG_FETCH_ERROR, ex.errorCode)
        assert(ex.message!!.contains("Expected image/svg+xml"))
    }

    @Test
    fun `throws SvgFetchException for empty response body`() {
        val mockClient = mock<OkHttpClient>()
        val mockCall = mock<Call>()
        val mockResponse = mock<Response>()
        val mockBody = mock<ResponseBody>()

        whenever(mockBody.string()).thenReturn(null)
        whenever(mockClient.newCall(any())).thenReturn(mockCall)
        whenever(mockCall.execute()).thenReturn(mockResponse)
        whenever(mockResponse.isSuccessful).thenReturn(true)
        whenever(mockResponse.header("Content-Type")).thenReturn("image/svg+xml")
        whenever(mockResponse.body).thenReturn(mockBody)

        val manager = NetworkManager(traceId, mockClient)

        val ex = assertFailsWith<VcRendererExceptions.SvgFetchException> {
            manager.fetchSvgAsText(testUrl)
        }

        assertEquals(SVG_FETCH_ERROR, ex.errorCode)
        assert(ex.message!!.contains("Empty response body"))
    }

    @Test
    fun `throws SvgFetchException for unexpected runtime error`() {
        val mockClient = mock<OkHttpClient>()
        val mockCall = mock<Call>()

        whenever(mockClient.newCall(any())).thenReturn(mockCall)
        whenever(mockCall.execute()).thenThrow(RuntimeException("Custom Error!"))

        val manager = NetworkManager(traceId, mockClient)

        val ex = assertFailsWith<VcRendererExceptions.SvgFetchException> {
            manager.fetchSvgAsText(testUrl)
        }

        assertEquals(SVG_FETCH_ERROR, ex.errorCode)
        assert(ex.message!!.contains("Custom Error!"))
    }

    @Test
    fun `returns SVG content successfully`() {
        val mockClient = mock<OkHttpClient>()
        val mockCall = mock<Call>()
        val mockResponse = mock<Response>()
        val mockBody = mock<ResponseBody>()
        val svgContent = "<svg><rect/></svg>"

        whenever(mockBody.string()).thenReturn(svgContent)
        whenever(mockClient.newCall(any())).thenReturn(mockCall)
        whenever(mockCall.execute()).thenReturn(mockResponse)
        whenever(mockResponse.isSuccessful).thenReturn(true)
        whenever(mockResponse.header("Content-Type")).thenReturn("image/svg+xml")
        whenever(mockResponse.body).thenReturn(mockBody)

        val manager = NetworkManager(traceId, mockClient)

        val result = manager.fetchSvgAsText(testUrl)

        assertNotNull(result)
        assertEquals(svgContent, result)
    }
}