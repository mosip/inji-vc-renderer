package io.mosip.injivcrenderer

import io.mosip.injivcrenderer.TemplatePreProcessor.Companion.BENEFITS_PLACEHOLDER_REGEX_PATTERN
import io.mosip.injivcrenderer.TemplatePreProcessor.Companion.FULL_ADDRESS_PLACEHOLDER_REGEX_PATTERN
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TemplatePreProcessorTest {
    private val templatePreProcessor = TemplatePreProcessor()

    @Test
    fun `preProcessSvgTemplate replaces only address placeholders`() {
        val vcJsonString = """{
            "credentialSubject": {
                "addressLine1": [
                    {
                        "language": "eng",
                        "value": "Address Line 1"
                    },
                    {
                        "language": "tam",
                        "value": "Addr1 Tam"
                    }
                ]
            }
        }"""
        val svgTemplate = "{{fullAddress1_eng}}"

        val expected = "Address Line 1"

        val result = templatePreProcessor.preProcessSvgTemplate(vcJsonString, svgTemplate)

        assertEquals(expected, result)
    }

    @Test
    fun `preProcessSvgTemplate replaces only benefits placeholders`() {

        val vcJsonString = """{
            "credentialSubject": {
                "benefits": [ "Benefits one, Benefits two"
                ]
            }
        }"""
        val svgTemplate = "{{benefits1}}"

        val expected = "Benefits one, Benefits two"

        val result = templatePreProcessor.preProcessSvgTemplate(vcJsonString, svgTemplate)

        assertEquals(expected, result)
    }

    @Test
    fun `preProcessSvgTemplate with no placeholders returns original template`() {

        val vcJsonString = """{"someField":"someValue"}"""
        val svgTemplate = "No placeholders here"

        val result = templatePreProcessor.preProcessSvgTemplate(vcJsonString, svgTemplate)

        assertEquals(svgTemplate, result)
    }

    @Test
    fun `testPlaceholders for Benefits`(){
        val svgTemplate = "<svg>{{benefits1}}--{{benefits2}}</svg>"
        val regexPattern = BENEFITS_PLACEHOLDER_REGEX_PATTERN.toRegex()

        val result = templatePreProcessor.getPlaceholdersList(regexPattern, svgTemplate)

        assertEquals(listOf("{{benefits1}}", "{{benefits2}}"), result)

    }

    @Test
    fun `testPlaceholders for Address`(){
        val svgTemplate = "<svg>{{fullAddress1_eng}}--{{fullAddress2_eng}}</svg>"
        val regexPattern = FULL_ADDRESS_PLACEHOLDER_REGEX_PATTERN.toRegex()

        val result = templatePreProcessor.getPlaceholdersList(regexPattern, svgTemplate)

        assertEquals(listOf("{{fullAddress1_eng}}", "{{fullAddress2_eng}}"), result)

    }



}