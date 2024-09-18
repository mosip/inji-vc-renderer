package io.mosip.injivcrenderer

import io.mosip.injivcrenderer.Utils.getValueBasedOnLanguage
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
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

    @Test
    fun testGetValueBasedOnLanguage_found() {
        val jsonArray = JSONArray()
        val jsonObject1 = JSONObject()
        jsonObject1.put("language", "eng")
        jsonObject1.put("value", "Hello")
        jsonArray.put(jsonObject1)

        val jsonObject2 = JSONObject()
        jsonObject2.put("language", "es")
        jsonObject2.put("value", "Hola")
        jsonArray.put(jsonObject2)

        val result = getValueBasedOnLanguage(jsonArray, "eng")

        assertEquals("Hello", result)
    }

    @Test
    fun testGetValueBasedOnLanguage_notFound() {
        val jsonArray = JSONArray()
        val jsonObject1 = JSONObject()
        jsonObject1.put("language", "eng")
        jsonObject1.put("value", "Hello")
        jsonArray.put(jsonObject1)

        val jsonObject2 = JSONObject()
        jsonObject2.put("language", "esp")
        jsonObject2.put("value", "Hola")
        jsonArray.put(jsonObject2)

        val result = getValueBasedOnLanguage(jsonArray, "fr")

        assertEquals("", result)
    }

    @Test
    fun testGetValueBasedOnLanguage_emptyArray() {
        val jsonArray = JSONArray()

        val result = getValueBasedOnLanguage(jsonArray, "en")

        assertEquals("", result)
    }

    @Test
    fun testGetValueBasedOnLanguage_defaultValue() {
        val jsonArray = JSONArray()
        val jsonObject = JSONObject()
        jsonObject.put("language", "de")
        jsonObject.put("value", "Hallo")
        jsonArray.put(jsonObject)

        val result = getValueBasedOnLanguage(jsonArray, "en")

        assertEquals("", result)
    }
}
