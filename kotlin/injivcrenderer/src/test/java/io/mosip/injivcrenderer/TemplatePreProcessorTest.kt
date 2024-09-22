package io.mosip.injivcrenderer

import io.mosip.injivcrenderer.TemplatePreProcessor.Companion.BENEFITS_PLACEHOLDER_REGEX_PATTERN
import io.mosip.injivcrenderer.TemplatePreProcessor.Companion.FULL_ADDRESS_PLACEHOLDER_REGEX_PATTERN
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TemplatePreProcessorTest {
    private val templatePreProcessor = TemplatePreProcessor()


    @Test
    fun `preprocess Locale Based Fields`() {
        val vcJsonString = """{
            "credentialSubject": {
                "gender": [
                    {
                        "language": "eng",
                        "value": "English Male"
                    },
                    {
                        "language": "tam",
                        "value": "Tamil Male"
                    }
                ]
            }
        }"""
        val svgTemplate = "{{credentialSubject/gender/eng}}"

        val expected = JSONObject("""{
            "credentialSubject": {
                "gender": 
                    {
                        "eng": "English Male",
                        "tam": "Tamil Male"
                    }
            }
        }""")

        val result = templatePreProcessor.preProcessSvgTemplate(vcJsonString, svgTemplate);

        assertEquals(expected.toString().trim(), result.toString().trim())
    }


    @Test
    fun `preProcess addressFields`() {
        val vcJsonString = """{
            "credentialSubject": {
                "addressLine1": [
                    {
                        "language": "eng",
                        "value": "Address Line 1"
                    },
                    {
                        "language": "fr",
                        "value": "Address Line1 French"
                    }
                ],
                "city": [
                    {
                        "language": "eng",
                        "value": "City"
                    },
                    {
                        "language": "fr",
                        "value": "City French"
                    }
                ]
            }
        }"""
        val svgTemplate = "{{credentialSubject/fullAddressLine1/eng}}"

        val expected = JSONObject("""{
            "credentialSubject": {
                "fullAddressLine1": {"eng":"Address Line 1, City"}
            }
        }""")

        val result = templatePreProcessor.preProcessSvgTemplate(vcJsonString, svgTemplate)

        assertEquals(expected.toString().trim(), result.toString().trim())
    }

    @Test
    fun `preProcess addressFields without address field objects`() {
        val vcJsonString = """{
            "credentialSubject": {
            }
        }"""
        val svgTemplate = "{{credentialSubject/fullAddressLine1/eng}}"

        val expected = JSONObject("""{
            "credentialSubject": {
            }
        }""")

        val result = templatePreProcessor.preProcessSvgTemplate(vcJsonString, svgTemplate)

        assertEquals(expected.toString().trim(), result.toString().trim())
    }

    @Test
    fun `preProcessSvgTemplate Benefits Field`() {

        val vcJsonString = """{
            "credentialSubject": {
                "benefits": [ "Benefits one, Benefits two"
                ]
            }
        }"""
        val svgTemplate = "{{credentialSubject/benefitsLine1}}"

        val expected = JSONObject("""{
            "credentialSubject": {
                "benefitsLine1":"Benefits one, Benefits two"
            }
        }""")

        val result = templatePreProcessor.preProcessSvgTemplate(vcJsonString, svgTemplate)

        assertEquals(expected.toString().trim(), result.toString().trim())
    }

    @Test
    fun `test getFieldNameFromPlaceholder`(){
        val placeholder = "{{credentialSubject/fullAddressLine1}}"
        val result = templatePreProcessor.getFieldNameFromPlaceholder(placeholder)
        assertEquals("fullAddressLine1", result)
    }

    @Test
    fun `test extractLanguageFromPlaceholder`(){
        val placeholder = "{{credentialSubject/fullAddressLine1/eng}}"
        val result = templatePreProcessor.extractLanguageFromPlaceholder(placeholder)
        assertEquals("eng", result)
    }

    @Test
    fun `test Address getPlaceholdersList`(){
        val svgTemplate = "Address Line 1:{{credentialSubject/fullAddressLine1/eng}}, Address Line 2: {{credentialSubject/fullAddressLine2/eng}}"
        val result = templatePreProcessor.getPlaceholdersList(FULL_ADDRESS_PLACEHOLDER_REGEX_PATTERN.toRegex(), svgTemplate)
        assertEquals(listOf("{{credentialSubject/fullAddressLine1/eng}}", "{{credentialSubject/fullAddressLine2/eng}}"), result)
    }

    @Test
    fun `test Benefits getPlaceholdersList`(){
        val svgTemplate = "Benefits Line 1:{{credentialSubject/benefitsLine1}}, Benefits Line 2: {{credentialSubject/benefitsLine2}}"
        val result = templatePreProcessor.getPlaceholdersList(BENEFITS_PLACEHOLDER_REGEX_PATTERN.toRegex(), svgTemplate)
        assertEquals(listOf("{{credentialSubject/benefitsLine1}}", "{{credentialSubject/benefitsLine2}}"), result)
    }

}
