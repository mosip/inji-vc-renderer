package io.mosip.injivcrenderer

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.mockito.MockitoAnnotations


class InjiVcRendererTest {

    private val renderer = InjiVcRenderer()


    @Test
    fun testReplaceAddress() {
        val jsonObject = mock<JSONObject>()
        val credentialSubject = mock<JSONObject>()
        val addressArray1 = mock<JSONArray>()
        val addressArray2 = mock<JSONArray>()

        `when`(jsonObject.optJSONObject("credentialSubject")).thenReturn(credentialSubject)

        `when`(credentialSubject.optJSONArray(InjiVcRenderer.ADDRESS_LINE_1)).thenReturn(addressArray1)
        `when`(addressArray1.length()).thenReturn(1)
        val addressLine1Object = mock<JSONObject>()
        `when`(addressArray1.optJSONObject(0)).thenReturn(addressLine1Object)
        `when`(addressLine1Object.optString("value", "")).thenReturn("123 Main St, 123 Main St, 123 Main St, 123 Main St")

        `when`(credentialSubject.optJSONArray(InjiVcRenderer.ADDRESS_LINE_2)).thenReturn(addressArray2)
        `when`(addressArray2.length()).thenReturn(1)
        val addressLine2Object = mock<JSONObject>()
        `when`(addressArray2.optJSONObject(0)).thenReturn(addressLine2Object)
        `when`(addressLine2Object.optString("value", "")).thenReturn("Apt 4B Test Region, Test Province_ENG")

        val svgTemplate = "<svg>{{fullAddress1}}{{fullAddress2}}</svg>"
        val expectedOutput = "<svg>123 Main St, 123 Main St, 123 Main St, 123 Main St, Apt 4B Test Region, Test Province_ENG</svg>"

        val result = renderer.replaceAddress(jsonObject, svgTemplate)

        assertEquals(expectedOutput, result)
    }

    @Test
    fun testReplaceAddressWithEmptyFields() {
        // Setup
        val jsonObject = mock<JSONObject>()
        val credentialSubject = mock<JSONObject>()
        val addressArray1 = mock<JSONArray>()
        val addressArray2 = mock<JSONArray>()

        `when`(jsonObject.optJSONObject("credentialSubject")).thenReturn(credentialSubject)

        `when`(credentialSubject.optJSONArray(InjiVcRenderer.ADDRESS_LINE_1)).thenReturn(addressArray1)
        `when`(credentialSubject.optJSONArray(InjiVcRenderer.ADDRESS_LINE_2)).thenReturn(addressArray2)

        `when`(addressArray1.length()).thenReturn(0)
        `when`(addressArray2.length()).thenReturn(0)

        val svgTemplate = "<svg>{{fullAddress1}}{{fullAddress2}}</svg>"
        val expectedOutput = "<svg>{{fullAddress1}}{{fullAddress2}}</svg>"

        val result = renderer.replaceAddress(jsonObject, svgTemplate)

        assertEquals(expectedOutput, result)
    }

    @Test
    fun testReplaceAddressWithPartialData() {

        val jsonObject = mock<JSONObject>()
        val credentialSubject = mock<JSONObject>()
        val addressArray1 = mock<JSONArray>()
        val addressArray2 = mock<JSONArray>()

        `when`(jsonObject.optJSONObject("credentialSubject")).thenReturn(credentialSubject)

        `when`(credentialSubject.optJSONArray(InjiVcRenderer.ADDRESS_LINE_1)).thenReturn(addressArray1)
        `when`(addressArray1.length()).thenReturn(1)
        val addressLine1Object = mock<JSONObject>()
        `when`(addressArray1.optJSONObject(0)).thenReturn(addressLine1Object)
        `when`(addressLine1Object.optString("value", "")).thenReturn("456 Elm St")

        // Define behavior for empty address line 2 array
        `when`(credentialSubject.optJSONArray(InjiVcRenderer.ADDRESS_LINE_2)).thenReturn(addressArray2)
        `when`(addressArray2.length()).thenReturn(0)

        `when`(addressArray2.optJSONObject(anyInt())).thenReturn(null)

        val svgTemplate = "<svg>{{fullAddress1}}{{fullAddress2}}</svg>"
        val expectedOutput = "<svg>456 Elm St{{fullAddress2}}</svg>"

        val result = renderer.replaceAddress(jsonObject, svgTemplate)

        assertEquals(expectedOutput, result)
    }

    @Test
    fun testReplaceAddressWithLongText() {

        val jsonObject = mock<JSONObject>()
        val credentialSubject = mock<JSONObject>()
        val addressArray1 = mock<JSONArray>()
        val addressArray2 = mock<JSONArray>()

        `when`(jsonObject.optJSONObject("credentialSubject")).thenReturn(credentialSubject)

        `when`(credentialSubject.optJSONArray(InjiVcRenderer.ADDRESS_LINE_1)).thenReturn(addressArray1)
        `when`(addressArray1.length()).thenReturn(1)
        val addressLine1Object = mock<JSONObject>()
        `when`(addressArray1.optJSONObject(0)).thenReturn(addressLine1Object)
        `when`(addressLine1Object.optString("value", "")).thenReturn("123456789012345678901234567890")

        `when`(credentialSubject.optJSONArray(InjiVcRenderer.ADDRESS_LINE_2)).thenReturn(addressArray2)
        `when`(addressArray2.length()).thenReturn(1)
        val addressLine2Object = mock<JSONObject>()
        `when`(addressArray2.optJSONObject(0)).thenReturn(addressLine2Object)
        `when`(addressLine2Object.optString("value", "")).thenReturn("987654321098765432109876543210")

        `when`(addressArray2.optJSONObject(anyInt())).thenReturn(null)

        val svgTemplate = "<svg>{{fullAddress1}}{{fullAddress2}}</svg>"
        val expectedOutput = "<svg>123456789012345678901234567890{{fullAddress2}}</svg>"

        val result = renderer.replaceAddress(jsonObject, svgTemplate)

        assertEquals(expectedOutput, result)
    }

    @Test
    fun testReplaceBenefits() {

        val jsonObject = mock<JSONObject>()
        val credentialSubject = mock<JSONObject>()
        val benefitsArray = mock<JSONArray>()

        `when`(jsonObject.optJSONObject("credentialSubject")).thenReturn(credentialSubject)
        `when`(credentialSubject.optJSONArray("benefits")).thenReturn(benefitsArray)
        `when`(benefitsArray.length()).thenReturn(2)
        `when`(benefitsArray.optString(0)).thenReturn("Benefit1")
        `when`(benefitsArray.optString(1)).thenReturn("Benefit2")

        val svgTemplate = "<svg>{{benefits1}}</svg>"
        val expectedOutput = "<svg>Benefit1,Benefit2</svg>"

        val result = renderer.replaceBenefits(jsonObject, svgTemplate)

        assertEquals(expectedOutput, result)
    }

    @Test
    fun testInvalidLocaledBasedValue() {
        val jsonObject = mock(JSONObject::class.java)
        val credentialSubject = mock(JSONObject::class.java)
        val genderArray = mock(JSONArray::class.java)

        `when`(jsonObject.optJSONObject("credentialSubject")).thenReturn(credentialSubject)
        `when`(credentialSubject.optJSONArray("gender")).thenReturn(genderArray)
        `when`(genderArray.length()).thenReturn(2)

        val genderEngObject = mock(JSONObject::class.java)
        `when`(genderEngObject.optString("value", "")).thenReturn("MLE")
        `when`(genderEngObject.optString("language", "")).thenReturn("eng")
        `when`(genderArray.optJSONObject(0)).thenReturn(genderEngObject)

        val result = renderer.replaceLocaleBasedValue("credentialSubject/gender_tam", jsonObject)
        assertNull(result)
    }

    @Test
    fun testReplaceBenefitsWithLongText() {

        val jsonObject = mock<JSONObject>()
        val credentialSubject = mock<JSONObject>()
        val benefitsArray = mock<JSONArray>()

        `when`(jsonObject.optJSONObject("credentialSubject")).thenReturn(credentialSubject)
        `when`(credentialSubject.optJSONArray("benefits")).thenReturn(benefitsArray)
        `when`(benefitsArray.length()).thenReturn(2)
        `when`(benefitsArray.optString(0)).thenReturn("Benefit1")
        `when`(benefitsArray.optString(1)).thenReturn("A very long benefit text that should be split into two parts")

        val svgTemplate = "<svg>{{benefits1}}</svg><svg>{{benefits2}}</svg>"
        val expectedOutput = "<svg>Benefit1,A very long benefit text that should be split </svg><svg>into two parts</svg>"

        val result = renderer.replaceBenefits(jsonObject, svgTemplate)

        assertEquals(expectedOutput, result)
    }


    @Test
    fun testWrapBasedOnCharacterLength() {

        val svgTemplate = "<svg>{{benefits1}}</svg><svg>{{benefits2}}</svg>"
        val dataToSplit = "Short text"
        val maxLength = 10
        val placeholdersList = listOf(InjiVcRenderer.BENEFITS_PLACEHOLDER_1, InjiVcRenderer.BENEFITS_PLACEHOLDER_2)
        val expectedOutput = "<svg>Short text</svg><svg>{{benefits2}}</svg>"

        val result = renderer.wrapBasedOnCharacterLength(svgTemplate, dataToSplit, maxLength, placeholdersList)

        assertEquals(expectedOutput, result)
    }



    @Test
    fun testWrapBasedOnCharacterLengthWithLongText() {

        val svgTemplate = "<svg>{{benefits1}}</svg><svg>{{benefits2}}</svg>"
        val dataToSplit = "A very long text that needs to be split"
        val maxLength = 20
        val placeholdersList = listOf(InjiVcRenderer.BENEFITS_PLACEHOLDER_1, InjiVcRenderer.BENEFITS_PLACEHOLDER_2)
        val expectedOutput = "<svg>A very long text tha</svg><svg>t needs to be split</svg>"

        val result = renderer.wrapBasedOnCharacterLength(svgTemplate, dataToSplit, maxLength, placeholdersList)

        assertEquals(expectedOutput, result)
    }

    @Test
    fun testWrapBasedOnCharacterLengthWithFewPlaceholders() {

        val svgTemplate = "<svg>{{benefits1}}</svg>"
        val dataToSplit = "Text that is split"
        val maxLength = 10
        val placeholdersList = listOf(InjiVcRenderer.BENEFITS_PLACEHOLDER_1)
        val expectedOutput = "<svg>Text that </svg>"

        val result = renderer.wrapBasedOnCharacterLength(svgTemplate, dataToSplit, maxLength, placeholdersList)

        assertEquals(expectedOutput, result)
    }

    @Test
    fun testGetValueFromDataBasicJson() {
        MockitoAnnotations.openMocks(this)

        val jsonObject = mock(JSONObject::class.java)
        val innerObject = mock(JSONObject::class.java)
        `when`(jsonObject.opt("a")).thenReturn(innerObject)
        `when`(innerObject.opt("b")).thenReturn(mock(JSONObject::class.java))
        val innerInnerObject = innerObject.opt("b") as JSONObject
        `when`(innerInnerObject.opt("c")).thenReturn("value")

        val result = renderer.getValueFromData("a/b/c", jsonObject)
        assertEquals("value", result)
    }


    @Test
    fun testGetValueFromDataInvalidKey() {
        MockitoAnnotations.openMocks(this)

        val jsonObject = mock(JSONObject::class.java)
        `when`(jsonObject.opt("a")).thenReturn(mock(JSONObject::class.java))
        val innerObject = jsonObject.opt("a") as JSONObject
        `when`(innerObject.opt("b")).thenReturn("value")

        val result = renderer.getValueFromData("a/b/c", jsonObject)
        assertNull(result)
    }

    @Test
    fun testGetValueFromDataIndexOutOfBounds() {
        MockitoAnnotations.openMocks(this)

        val jsonObject = mock(JSONObject::class.java)
        val jsonArray = mock(JSONArray::class.java)
        `when`(jsonObject.opt("arr")).thenReturn(jsonArray)
        `when`(jsonArray.length()).thenReturn(2)

        val result = renderer.getValueFromData("arr/5", jsonObject)
        assertNull(result)
    }



    @Test
    fun testGetValueFromDataDeeplyNestedJson() {
        MockitoAnnotations.openMocks(this)

        val jsonObject = mock(JSONObject::class.java)
        val level1Object = mock(JSONObject::class.java)
        val level2Object = mock(JSONObject::class.java)
        val level3Object = mock(JSONObject::class.java)
        val level4Object = mock(JSONObject::class.java)
        `when`(jsonObject.opt("level1")).thenReturn(level1Object)
        `when`(level1Object.opt("level2")).thenReturn(level2Object)
        `when`(level2Object.opt("level3")).thenReturn(level3Object)
        `when`(level3Object.opt("level4")).thenReturn(level4Object)
        `when`(level4Object.opt("target")).thenReturn("deepValue")

        val result = renderer.getValueFromData("level1/level2/level3/level4/target", jsonObject)
        assertEquals("deepValue", result)
    }



    @Test
    fun testGetValueFromDataEmptyJson() {
        MockitoAnnotations.openMocks(this)

        val jsonObject = mock(JSONObject::class.java)
        `when`(jsonObject.opt("any")).thenReturn(null)

        val result = renderer.getValueFromData("any/path", jsonObject)
        assertNull(result)
    }


}
