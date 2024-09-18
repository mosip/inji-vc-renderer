package io.mosip.injivcrenderer

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class PreProcessArrayTest {
    private val templatePreProcessor = TemplatePreProcessor()


    @Test
    fun testTransformArrayFieldsIntoMultiline_Success() {
        val jsonObject = mock(JSONObject::class.java)
        val benefitsArray = mock(JSONArray::class.java)
        val svgTemplate = "<svg>{{benefits1}}{{benefits2}}</svg>"
        val multiLineProperties = MultiLineProperties(listOf("{{benefits1}}", "{{benefits2}}"), 10,"benefits")

        `when`(jsonObject.optJSONObject("credentialSubject")).thenReturn(jsonObject)
        `when`(jsonObject.optJSONArray(multiLineProperties.fieldName)).thenReturn(benefitsArray)

        `when`(benefitsArray.length()).thenReturn(2)
        `when`(benefitsArray.optString(0)).thenReturn("Benefits10")
        `when`(benefitsArray.optString(1)).thenReturn("Benefits20")

        val result = templatePreProcessor.transformArrayFieldsIntoMultiline(jsonObject, svgTemplate, multiLineProperties)

        val expectedOutput = "<svg>Benefits10,Benefits2</svg>"
        assertEquals(expectedOutput, result)
    }

    @Test
    fun testTransformArrayFieldsIntoMultiline_EmptyArray() {
        val jsonObject = mock(JSONObject::class.java)
        val benefitsArray = mock(JSONArray::class.java)
        val svgTemplate = "<svg>{{benefits1}}{{benefits2}}</svg>"
        val multiLineProperties = MultiLineProperties(listOf("{{benefits1}}", "{{benefits2}}"), 10,"benefits")

        `when`(jsonObject.optJSONObject("credentialSubject")).thenReturn(jsonObject)
        `when`(jsonObject.optJSONArray(multiLineProperties.fieldName)).thenReturn(benefitsArray)

        `when`(benefitsArray.length()).thenReturn(2)

        val result = templatePreProcessor.transformArrayFieldsIntoMultiline(jsonObject, svgTemplate, multiLineProperties)

        val expectedOutput = "<svg>{{benefits1}}{{benefits2}}</svg>"
        assertEquals(expectedOutput, result)
    }


    @Test
    fun testTransformArrayFieldsIntoMultiline_NonExistentField() {
        val jsonObject = mock(JSONObject::class.java)
        val svgTemplate = "<svg>{{benefits1}}{{benefits2}}</svg>"
        val multiLineProperties = MultiLineProperties(listOf("{{benefits1}}", "{{benefits2}}"), 10,"benefits")

        `when`(jsonObject.optJSONObject("credentialSubject")).thenReturn(jsonObject)

        val result = templatePreProcessor.transformArrayFieldsIntoMultiline(jsonObject, svgTemplate, multiLineProperties)

        val expectedOutput = "<svg>{{benefits1}}{{benefits2}}</svg>"
        assertEquals(expectedOutput, result)
    }

    @Test
    fun testWrapBasedOnCharacterLength_Success() {
        val dataToSplit = "value1,value2,value3"
        val maxLength = 10
        val placeholders = listOf("{{benefits1}}", "{{benefits2}}")
        val svgTemplate = "<text>{{benefits1}}</text><text>{{benefits2}}</text>"

        val result = templatePreProcessor.wrapBasedOnCharacterLength(svgTemplate, dataToSplit, maxLength, placeholders)
        assertEquals("<text>value1,val</text><text>ue2,value3</text>", result)
    }

    @Test
    fun testWrapBasedOnCharacterLength_TooFewPlaceholders() {
        val dataToSplit = "value1,value2,value3"
        val maxLength = 10
        val placeholders = listOf("{{benefits1}}")
        val svgTemplate = "<text>{{benefits1}}</text><text>{{benefits2}}</text>"

        val result = templatePreProcessor.wrapBasedOnCharacterLength(svgTemplate, dataToSplit, maxLength, placeholders)
        assertEquals("<text>value1,val</text><text>{{benefits2}}</text>", result)
    }

    @Test
    fun testWrapBasedOnCharacterLength_NoData() {
        val dataToSplit = ""
        val maxLength = 10
        val placeholders = listOf("{{benefits1}}", "{{benefits2}}")
        val svgTemplate = "<text>{{benefits1}}</text><text>{{benefits2}}</text>"

        val result = templatePreProcessor.wrapBasedOnCharacterLength(svgTemplate, dataToSplit, maxLength, placeholders)
        assertEquals(svgTemplate, result)
    }
}