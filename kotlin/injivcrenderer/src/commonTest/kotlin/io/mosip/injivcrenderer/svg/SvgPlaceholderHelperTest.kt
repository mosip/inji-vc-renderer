package io.mosip.injivcrenderer.svg

import io.mosip.injivcrenderer.svg.SvgPlaceholderHelper.preserveRenderProperty
import io.mosip.injivcrenderer.svg.SvgPlaceholderHelper.replacePlaceholders
import org.json.JSONObject
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SvgPlaceholderHelperTest {


    @Test
    fun `replace Fields with locale in template and vc`() {

        val svgTemplateWithLocale = "<svg lang=\"eng\">{{/credentialSubject/gender}}##{{/credentialSubject/fullName}}</svg>"

        val processedJson = JSONObject("""{
            "credentialSubject": {
                "gender":[
                    {
                        "language": "eng",
                        "value": "English Male"
                    }
                ],
                "fullName": "John"
            }
        }""")

        val expected = "<svg lang=\"eng\">English Male##John</svg>"

        val result = replacePlaceholders(svgTemplateWithLocale, processedJson);
        assertEquals(expected, result)
    }

    @Test
    fun `replace Fields with locale in template and not in vc`() { //take

        val svgTemplateWithLocale = "<svg lang=\"eng\">{{/credentialSubject/gender}}</svg>"

        val processedJson = JSONObject("""{
            "credentialSubject": {
                "gender": "Male"
            }
        }""")

        val expected = "<svg lang=\"eng\">Male</svg>"

        val result = replacePlaceholders(svgTemplateWithLocale, processedJson);
        assertEquals(expected, result)
    }

    @Test
    fun `replace Fields with locale in not in template and in vc`() { //take default eng if not available take first one

        val svgTemplateWithLocale = "<svg>{{/credentialSubject/gender}}</svg>"

        val processedJson = JSONObject("""{
            "credentialSubject": {
                "gender":[
                    {
                        "language": "eng",
                        "value": "English Male"
                    },
                    {
                        "language": "fr",
                        "value": "French Male"
                    }
                ]
            }
        }""")

        val expected = "<svg>English Male</svg>"

        val result = replacePlaceholders(svgTemplateWithLocale, processedJson);
        assertEquals(expected, result)
    }

    @Test
    fun `replace Fields with locale is not in template and not in vc`() {

        val svgTemplateWithLocale = "<svg>{{/credentialSubject/gender}}</svg>"

        val processedJson = JSONObject("""{
            "credentialSubject": {
                "gender": "Male"
            }
        }""")

        val expected = "<svg>Male</svg>"

        val result = replacePlaceholders(svgTemplateWithLocale, processedJson);
        assertEquals(expected, result)
    }

    @Test
    fun `replace Fields with locale in template and which is not available in vc but has eng in it - default to english`() {

        val svgTemplateWithLocale = "<svg lang=\"fr\">{{/credentialSubject/gender}}</svg>"

        val processedJson = JSONObject("""{
            "credentialSubject": {
                "gender":[
                    {
                        "language": "tam",
                        "value": "Tamil Male"
                    },
                    {
                        "language": "eng",
                        "value": "English Male"
                    }
                ]
            }
        }""")

        val expected = "<svg lang=\"fr\">English Male</svg>"

        val result = replacePlaceholders(svgTemplateWithLocale, processedJson);
        assertEquals(expected, result)
    }

    @Test
    fun `replace Fields with locale in template and which is not available in vc not even eng in it - default to first item`() {

        val svgTemplateWithLocale = "<svg lang=\"fr\">{{/credentialSubject/gender}}</svg>"

        val processedJson = JSONObject("""{
            "credentialSubject": {
                "gender":[
                    {
                        "language": "tam",
                        "value": "Tamil Male"
                    },
                    {
                        "language": "spanish",
                        "value": "Spanish Male"
                    }
                ]
            }
        }""")

        val expected = "<svg lang=\"fr\">Tamil Male</svg>"

        val result = replacePlaceholders(svgTemplateWithLocale, processedJson);
        assertEquals(expected, result)
    }

    @Test
    fun `replace benefits array into multiline in SVG Template`() {

        val svgTemplate = "<svg width=\"340\" height=\"234\">\n<text>{{/credentialSubject/benefits}}</text></svg>"

        val vcJson = JSONObject("""
              { 
                "credentialSubject": {
                    "fullName": "John Doe",
                    "benefits": [
                        "Item 1 is on the list",
                        "Item 2 is on the list",
                        "Item 3 is on the list",
                        "Item 4 is on the list",
                        "Item 5 is on the list",
                        "Item 6 is on the list"
                    ]
                },
                "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                        "id": "https://degree.example/credential-templates/benefits-mutliline.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                      }
                  }
              }
        """)

        val result = replacePlaceholders(svgTemplate, vcJson)

        assertEquals("<svg width=\"340\" height=\"234\">\n<text><tspan x=\"0\" dy=\"1.2em\">Item 1 is on the list, Item 2 is on the</tspan><tspan x=\"0\" dy=\"1.2em\">list, Item 3 is on the list, Item 4 is on</tspan><tspan x=\"0\" dy=\"1.2em\">the list, Item 5 is on the list, Item 6 is</tspan><tspan x=\"0\" dy=\"1.2em\">on the list</tspan></text></svg>", result)
    }

    @Test
    fun `replace concatenatedAddress fields with locale in template and in vc`() {
        val svgTemplate = "<svg lang=\"eng\">{{/credentialSubject/concatenatedAddress}}</svg>"

        val vcJson = JSONObject("""{
            "credentialSubject": {
                "addressLine1": [
                    {
                        "language": "eng",
                        "value": "TEST_ADDRESS_LINE_1eng"
                    },
                   {
                        "language": "fr",
                        "value": "TEST_ADDRESS_LINE_1fr"
                    },
                ],
                "addressLine2": [
                    {
                        "language": "eng",
                        "value": "TEST_ADDRESS_LINE_2eng"
                    }
                ],
                "city": [
                    {
                        "language": "eng",
                        "value": "TEST_CITYeng"
                    }
                ],
                "province": [
                    {
                        "language": "eng",
                        "value": "TEST_PROVINCEeng"
                    }
                ],
                "region": [
                    {
                        "language": "eng",
                        "value": "TEST_REGIONeng"
                    }
                ],
                "postalCode": [
                    {
                        "language": "eng",
                        "value": "TEST_POSTAL_CODEeng"
                    }
                ]
            },
            "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                        "id": "https://degree.example/credential-templates/address-concatenated-no-locale.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                      }
                  }
              }
        }""")

        val result = replacePlaceholders(svgTemplate, vcJson)
        assertEquals(
                "<svg lang=\"eng\"><tspan x=\"0\" dy=\"1.2em\"></tspan><tspan x=\"0\" dy=\"1.2em\">TEST_ADDRESS_LINE_1eng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_ADDRESS_LINE_2eng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_CITYeng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_PROVINCEeng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_REGIONeng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_POSTAL_CODEeng</tspan></svg>", result)

    }

    @Test
    fun `replace concatenatedAddress fields without locale in template and in vc`() {
        val svgTemplate = "<svg>{{/credentialSubject/concatenatedAddress}}</svg>"

        val vcJson = JSONObject("""{
            "credentialSubject": {
                "addressLine1": [
                    {
                        "language": "eng",
                        "value": "TEST_ADDRESS_LINE_1eng"
                    },
                   {
                        "language": "fr",
                        "value": "TEST_ADDRESS_LINE_1fr"
                    },
                ],
                "addressLine2": [
                    {
                        "language": "eng",
                        "value": "TEST_ADDRESS_LINE_2eng"
                    }
                ],
                "city": [
                    {
                        "language": "eng",
                        "value": "TEST_CITYeng"
                    }
                ],
                "province": [
                    {
                        "language": "eng",
                        "value": "TEST_PROVINCEeng"
                    }
                ],
                "region": [
                    {
                        "language": "eng",
                        "value": "TEST_REGIONeng"
                    }
                ],
                "postalCode": [
                    {
                        "language": "eng",
                        "value": "TEST_POSTAL_CODEeng"
                    }
                ]
            },
            "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                        "id": "https://degree.example/credential-templates/address-concatenated-no-locale.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                      }
                  }
              }
        }""")

        val result = replacePlaceholders(svgTemplate, vcJson)
        assertEquals(
            "<svg><tspan x=\"0\" dy=\"1.2em\"></tspan><tspan x=\"0\" dy=\"1.2em\">TEST_ADDRESS_LINE_1eng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_ADDRESS_LINE_2eng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_CITYeng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_PROVINCEeng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_REGIONeng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_POSTAL_CODEeng</tspan></svg>", result)

    }

    @Test
    fun `replace concatenatedAddress fields without few fields locale in vc and not in template`() {
        val svgTemplate = "<svg>{{/credentialSubject/concatenatedAddress}}</svg>"

        val vcJson = JSONObject("""{
            "credentialSubject": {
                "addressLine1": [
                    {
                        "language": "eng",
                        "value": "TEST_ADDRESS_LINE_1eng"
                    },
                   {
                        "language": "fr",
                        "value": "TEST_ADDRESS_LINE_1fr"
                    },
                ],
                "addressLine2": "TEST_ADDRESS_LINE_2eng",
                "city": [
                    {
                        "language": "eng",
                        "value": "TEST_CITYeng"
                    }
                ],
                "province": [
                    {
                        "language": "eng",
                        "value": "TEST_PROVINCEeng"
                    }
                ],
                "region": [
                    {
                        "language": "eng",
                        "value": "TEST_REGIONeng"
                    }
                ],
                "postalCode": [
                    {
                        "language": "eng",
                        "value": "TEST_POSTAL_CODEeng"
                    }
                ]
            },
            "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                        "id": "https://degree.example/credential-templates/address-concatenated-no-locale.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                      }
                  }
              }
        }""")

        val result = replacePlaceholders(svgTemplate, vcJson)
        assertEquals(
            "<svg><tspan x=\"0\" dy=\"1.2em\"></tspan><tspan x=\"0\" dy=\"1.2em\">TEST_ADDRESS_LINE_1eng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_ADDRESS_LINE_2eng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_CITYeng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_PROVINCEeng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_REGIONeng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_POSTAL_CODEeng</tspan></svg>", result)

    }
}