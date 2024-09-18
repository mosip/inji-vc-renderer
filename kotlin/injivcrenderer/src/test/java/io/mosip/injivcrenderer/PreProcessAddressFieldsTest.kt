package io.mosip.injivcrenderer

import io.mosip.injivcrenderer.TemplatePreProcessor.Companion.ADDRESS_LINE_1
import io.mosip.injivcrenderer.TemplatePreProcessor.Companion.ADDRESS_LINE_2
import io.mosip.injivcrenderer.TemplatePreProcessor.Companion.CITY
import io.mosip.injivcrenderer.TemplatePreProcessor.Companion.POSTAL_CODE
import io.mosip.injivcrenderer.TemplatePreProcessor.Companion.PROVINCE
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PreProcessAddressFieldsTest {

    private val templatePreProcessor = TemplatePreProcessor()
    @Test
    fun `transformAddressFieldsIntoMultiline with valid address data`() {
        val jsonObject = JSONObject().apply {
            put("credentialSubject", JSONObject().apply {
                put(ADDRESS_LINE_1, JSONArray().apply {
                    put(JSONObject().apply {
                        put("value", "123 Main St")
                        put("language", "eng")
                    })
                })
                put(ADDRESS_LINE_2, JSONArray().apply {
                    put(JSONObject().apply {
                        put("value", "Apt 4B")
                        put("language", "eng")
                    })
                })
                put(CITY, JSONArray().apply {
                    put(JSONObject().apply {
                        put("value", "Springfield")
                        put("language", "eng")
                    })
                })
                put(PROVINCE, JSONArray().apply {
                    put(JSONObject().apply {
                        put("value", "IL")
                        put("language", "eng")
                    })
                })
                put(POSTAL_CODE, JSONArray().apply {
                    put(JSONObject().apply {
                        put("value", "62704")
                        put("language", "eng")
                    })
                })
            })
        }
        val svgTemplate = "Address: {{fullAddress1_eng}}"
        val multiLineProperties = MultiLineProperties(maxCharacterLength = 50, placeholders = listOf("{{fullAddress1_eng}}"))

        val result = templatePreProcessor.transformAddressFieldsIntoMultiline(jsonObject, svgTemplate, multiLineProperties)

        val expected = "Address: 123 Main St, Apt 4B, Springfield, IL, 62704"
        assertEquals(expected, result)
    }

    @Test
    fun `transformAddressFieldsIntoMultiline with not matching locale`() {
        val jsonObject = JSONObject().apply {
            put("credentialSubject", JSONObject().apply {
                put(ADDRESS_LINE_1, JSONArray().apply {
                    put(JSONObject().apply {
                        put("value", "123 Main St")
                        put("language", "eng")
                    })
                })
                put(ADDRESS_LINE_2, JSONArray().apply {
                    put(JSONObject().apply {
                        put("value", "Apt 4B")
                        put("language", "eng")
                    })
                })
            })
        }
        val svgTemplate = "Address: {{fullAddress1_tam}}"
        val multiLineProperties = MultiLineProperties(maxCharacterLength = 50, placeholders = listOf("{{fullAddress1_tam}}"))

        val result = templatePreProcessor.transformAddressFieldsIntoMultiline(jsonObject, svgTemplate, multiLineProperties)

        val expected = "Address: {{fullAddress1_tam}}"
        assertEquals(expected, result)
    }

    @Test
    fun `transformAddressFieldsIntoMultiline with no address fields`() {
        val jsonObject = JSONObject()
        val svgTemplate = "Address: {{fullAddress1_eng}}"
        val multiLineProperties = MultiLineProperties(maxCharacterLength = 50, placeholders = listOf("{{fullAddress1_eng}}"))

        val result = templatePreProcessor.transformAddressFieldsIntoMultiline(jsonObject, svgTemplate, multiLineProperties)

        val expected = "Address: {{fullAddress1_eng}}"
        assertEquals(expected, result)
    }
}