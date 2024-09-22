package io.mosip.injivcrenderer

import org.json.JSONObject
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InjiVcRendererTest {

    private val injivcRenderer = InjiVcRenderer()


    @Test
    fun `replace Locale Based Fields`() {

        val svgTemplateWithLocale = "<svg>{{credentialSubject/gender/eng}}</svg>"
        val svgTemplateWithoutLocale = "<svg>{{credentialSubject/gender}}</svg>"
        val svgTemplateWithUnavailableLocale = "<svg>{{credentialSubject/gender}}</svg>"
        val svgTemplateWithInvalidKey = "<svg>{{credentialSubject/genders}}</svg>"

        val processedJson = JSONObject("""{
            "credentialSubject": {
                "gender": 
                    {
                        "eng": "English Male",
                        "tam": "Tamil Male"
                    }
            }
        }""")

        val expected = "<svg>English Male</svg>"

        val result1 = injivcRenderer.replacePlaceholders(svgTemplateWithLocale, processedJson);
        assertEquals(expected, result1)

        val result2 = injivcRenderer.replacePlaceholders(svgTemplateWithoutLocale, processedJson);
        assertEquals(expected, result2)

        val result3 = injivcRenderer.replacePlaceholders(svgTemplateWithUnavailableLocale, processedJson);
        assertEquals(expected, result3)

        val result4 = injivcRenderer.replacePlaceholders(svgTemplateWithInvalidKey, processedJson);
        assertEquals("<svg></svg>", result4)
    }

    @Test
    fun `replace addressFields`() {
        val svgTemplateWithLocale = "<svg>{{credentialSubject/fullAddressLine1/eng}}</svg>"
        val svgTemplateWithoutLocale = "<svg>{{credentialSubject/fullAddressLine1}}</svg>"
        val svgTemplateWithUnavailableLocale = "<svg>{{credentialSubject/fullAddressLine1/fr}}</svg>"

        val processedJson = JSONObject("""{
            "credentialSubject": {
                "fullAddressLine1": {"eng":"Address Line 1, City"}
            }
        }""")

        val result1 = injivcRenderer.replacePlaceholders(svgTemplateWithLocale, processedJson)
        assertEquals("<svg>Address Line 1, City</svg>", result1)

        val result2 = injivcRenderer.replacePlaceholders(svgTemplateWithoutLocale, processedJson)
        assertEquals("<svg>Address Line 1, City</svg>", result2)

        val result3 = injivcRenderer.replacePlaceholders(svgTemplateWithUnavailableLocale, processedJson)
        assertEquals("<svg>Address Line 1, City</svg>", result3)

    }

    @Test
    fun `replace addressFields with empty fullAddress`() {
        val svgTemplate = "<svg>{{credentialSubject/fullAddressLine1/eng}}</svg>"

        val processedJson = JSONObject("""{
            "credentialSubject": {
                "fullAddressLine1": {}
            }
        }""")

        val result = injivcRenderer.replacePlaceholders(svgTemplate, processedJson)
        assertEquals("<svg></svg>", result)

    }

    @Test
    fun `replace benefits`() {
        val svgTemplateWithLocale = "<svg>{{credentialSubject/benefitsLine1}}</svg>"

        val processedJson = JSONObject("""{
            "credentialSubject": {
                "benefitsLine1":"Benefits one, Benefits two"
            }
        }""")

        val result = injivcRenderer.replacePlaceholders(svgTemplateWithLocale, processedJson)
        assertEquals("<svg>Benefits one, Benefits two</svg>", result)

    }

    @Test
    fun `replace addressFields with empty benefits`() {
        val svgTemplate = "<svg>{{credentialSubject/benefitsLine1}}</svg>"

        val processedJson = JSONObject("""{
            "credentialSubject": {
                "benenfitsLine1": []
            }
        }""")

        val result = injivcRenderer.replacePlaceholders(svgTemplate, processedJson)
        assertEquals("<svg></svg>", result)

    }


    @Test
    fun `renderSvg handles missing renderMethod`() {
        val vcJsonString = """{}"""

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals("", result)
    }

    @Test
    fun `renderSvg handles invalid JSON input`() {
        val vcJsonString = """{ "renderMethod": [ "invalid" ] }"""

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals("", result)
    }


}
