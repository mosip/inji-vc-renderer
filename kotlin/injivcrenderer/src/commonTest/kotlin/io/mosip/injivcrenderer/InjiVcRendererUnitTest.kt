package io.mosip.injivcrenderer


import io.mosip.injivcrenderer.svg.SvgPlaceholderHelper.replacePlaceholders
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.runner.RunWith
import org.mockito.Mockito.mockConstruction
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InjiVcRendererTest {

    private lateinit var injivcRenderer: InjiVcRenderer

    private lateinit var mockConstruction: AutoCloseable


    @Before
    fun setup() {
        mockConstruction = mockConstruction(NetworkHandler::class.java) { mock, _ ->
            whenever(mock.fetchSvgAsText(any())).thenAnswer { invocation ->
                val url = invocation.arguments[0] as String
                when {
                    url.contains("front") -> "<svg>Full Name - {{/credentialSubject/fullName}}</svg>"
                    url.contains("rear") -> "<svg>Email - {{/credentialSubject/email}}</svg>"
                    url.contains("with-render-property") -> "<svg>Details - {{/credentialSubject/fullName}}****{{/issuer}}****{{/validFrom}}***{{/credentialSubject/phone}}</svg>"
                    url.contains("address.svg") -> "<svg>Address : {{/address}}****{{/city}}****{{/pincode}}***</svg>"
                    url.contains("benefits.svg") -> "<svg>{{/credentialSubject/benefits}}</svg>"
                    url.contains("benefits-mutliline.svg") -> "<svg width=\"340\" height=\"234\">\n<text>{{/credentialSubject/benefits}}</text></svg>"
                    url.contains("address-concatenated.svg") -> "<svg lang=\"eng\">{{/credentialSubject/concatenatedAddress}}</svg>"
                    url.contains("address-concatenated-no-locale.svg") -> "<svg >{{/credentialSubject/concatenatedAddress}}</svg>"
                    else -> "<svg>default</svg>"
                }
            }
        }
        injivcRenderer = InjiVcRenderer() // uses its internal NetworkHandler
    }

    @After
    fun tearDown() {
        mockConstruction.close()
    }





    @Test
    fun `replace Locale Based Fields`() {

        val svgTemplateWithLocale = "<svg>{{/credentialSubject/gender/eng}}</svg>"
        val svgTemplateWithoutLocale = "<svg>{{/credentialSubject/gender}}</svg>"
        val svgTemplateWithUnavailableLocale = "<svg>{{/credentialSubject/gender}}</svg>"
        val svgTemplateWithInvalidKey = "<svg>{{/credentialSubject/genders}}</svg>"

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

        val result1 = replacePlaceholders(svgTemplateWithLocale, processedJson);
        assertEquals(expected, result1)

        val result2 = replacePlaceholders(svgTemplateWithoutLocale, processedJson);
        assertEquals(expected, result2)

        val result3 = replacePlaceholders(svgTemplateWithUnavailableLocale, processedJson);
        assertEquals(expected, result3)

        val result4 = replacePlaceholders(svgTemplateWithInvalidKey, processedJson);
        assertEquals("<svg>-</svg>", result4)
    }

    @Test
    fun `replace addressFields`() {
        val svgTemplateWithLocale = "<svg>{{/credentialSubject/fullAddressLine1/eng}}</svg>"
        val svgTemplateWithoutLocale = "<svg>{{/credentialSubject/fullAddressLine1}}</svg>"
        val svgTemplateWithUnavailableLocale = "<svg>{{/credentialSubject/fullAddressLine1/fr}}</svg>"

        val processedJson = JSONObject("""{
            "credentialSubject": {
                "fullAddressLine1": {"eng":"Address Line 1, City"}
            }
        }""")

        val result1 = replacePlaceholders(svgTemplateWithLocale, processedJson)
        assertEquals("<svg>Address Line 1, City</svg>", result1)

        val result2 = replacePlaceholders(svgTemplateWithoutLocale, processedJson)
        assertEquals("<svg>Address Line 1, City</svg>", result2)

        val result3 = replacePlaceholders(svgTemplateWithUnavailableLocale, processedJson)
        assertEquals("<svg>Address Line 1, City</svg>", result3)

    }

    @Test
    fun `replace concatenatedAddress fields with locale`() {


        val vcJson = """{
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
                        "id": "https://degree.example/credential-templates/address-concatenated.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                      }
                  }
              }
        }"""

        val result = injivcRenderer.renderSvg(vcJson)
        assertEquals(
            listOf(
            "<svg lang=\"eng\"><tspan x=\"0\" dy=\"1.2em\"></tspan><tspan x=\"0\" dy=\"1.2em\">TEST_ADDRESS_LINE_1eng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_ADDRESS_LINE_2eng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_CITYeng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_PROVINCEeng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_REGIONeng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_POSTAL_CODEeng</tspan></svg>"), result)

    }

    @Test
    fun `replace concatenatedAddress fields without locale`() {


        val vcJson = """{
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
        }"""

        val result = injivcRenderer.renderSvg(vcJson)
        assertEquals(
            listOf(
                "<svg ><tspan x=\"0\" dy=\"1.2em\"></tspan><tspan x=\"0\" dy=\"1.2em\">TEST_ADDRESS_LINE_1eng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_ADDRESS_LINE_2eng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_CITYeng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_PROVINCEeng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_REGIONeng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_POSTAL_CODEeng</tspan></svg>"), result)

    }

    @Test
    fun `replace concatenatedAddress fields with address fields as plain string`() {

        val vcJson = """{
            "credentialSubject": {
                "addressLine1": "TEST_ADDRESS_LINE_1eng",
                "addressLine2": "TEST_ADDRESS_LINE_2eng",
                "city": "TEST_CITYeng",
                "province": "TEST_PROVINCEeng",
                "region": "TEST_REGIONeng",
                "postalCode": "TEST_POSTAL_CODEeng"
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
        }"""

        val result = injivcRenderer.renderSvg(vcJson)
        assertEquals(
            listOf(
                "<svg ><tspan x=\"0\" dy=\"1.2em\"></tspan><tspan x=\"0\" dy=\"1.2em\">TEST_ADDRESS_LINE_1eng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_ADDRESS_LINE_2eng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_CITYeng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_PROVINCEeng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_REGIONeng,</tspan><tspan x=\"0\" dy=\"1.2em\">TEST_POSTAL_CODEeng</tspan></svg>"), result)

    }

    @Test
    fun `replace addressFields with empty fullAddress`() {
        val svgTemplate = "<svg>{{/credentialSubject/fullAddressLine1/eng}}</svg>"

        val processedJson = JSONObject("""{
            "credentialSubject": {
                "fullAddressLine1": {}
            }
        }""")

        val result = replacePlaceholders(svgTemplate, processedJson)
        assertEquals("<svg>-</svg>", result)

    }


    @Test
    fun `replace benefits array in SVG Template`() {
        val vcJsonString = """
              { 
                "credentialSubject": {
                    "fullName": "John Doe",
                    "benefits": [
                        "Critical Surgery",
                        "Full body checkup"
                    ]
                },
                "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                        "id": "https://degree.example/credential-templates/benefits.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                      }
                  }
              }
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals(listOf("<svg><tspan x=\"0\" dy=\"1.2em\">Critical</tspan><tspan x=\"0\" dy=\"1.2em\">Surgery,</tspan><tspan x=\"0\" dy=\"1.2em\">Full body</tspan><tspan x=\"0\" dy=\"1.2em\">checkup</tspan></svg>"), result)
    }

    @Test
    fun `replace benefits array into multiline in SVG Template`() {
        // SVG Template - <svg><text>{{/credentialSubject/benefits}}</text></svg>
        //Item 1 is on the list, Item 2 is on the list
        val vcJsonString = """
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
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals(listOf("<svg width=\"340\" height=\"234\">\n<text><tspan x=\"0\" dy=\"1.2em\">Item 1 is on the list, Item 2 is on the</tspan><tspan x=\"0\" dy=\"1.2em\">list, Item 3 is on the list, Item 4 is on</tspan><tspan x=\"0\" dy=\"1.2em\">the list, Item 5 is on the list, Item 6 is</tspan><tspan x=\"0\" dy=\"1.2em\">on the list</tspan></text></svg>"), result)
    }

    @Test
    fun `replace addressFields with empty benefits`() {
        val svgTemplate = "<svg>{{/credentialSubject/benefitsLine1}}</svg>"

        val processedJson = JSONObject("""{
            "credentialSubject": {
                "benenfitsLine1": []
            }
        }""")

        val result = replacePlaceholders(svgTemplate, processedJson)
        assertEquals("<svg>-</svg>", result)

    }


    @Test
    fun `renderSvg handles missing renderMethod`() {
        val vcJsonString = """{}"""

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `renderSvg handles invalid JSON input`() {
        val vcJsonString = """{ "renderMethod": [ "invalid" ] }"""

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertTrue(result.isEmpty())
    }



    @Test
    fun `renderSvg handles without renderMethod field`() {
        val vcJsonString = """
            { 
                "someField": "someValue" 
            }
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `renderSvg handles with renderMethod field as empty object`() {
        val vcJsonString = """
              { "renderMethod": {
                }
              }
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `renderSvg handles with renderMethod field as empty array`() {
        val vcJsonString = """
              {
                "renderMethod": [
                ]
            }
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `renderSvg handles with renderMethod as array - renderSuite is invalid`() {
        val vcJsonString = """
              {
                "renderMethod": [
                   { "type": "TemplateRenderMethod", "renderSuite": "invalid-suite" }
                ]
            }
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `renderSvg handles with renderMethod as array - type is invalid`() {
        val vcJsonString = """
              {
                "renderMethod": [
                   { "type": "invalid", "renderSuite": "svg-mustache" }
                ]
            }
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `renderSvg handles with renderMethod as json - renderSuite is invalid`() {
        val vcJsonString = """
              {
                "renderMethod": { "type": "TemplateRenderMethod", "renderSuite": "invalid-suite" }
            }
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `renderSvg handles with renderMethod as json - type is invalid`() {
        val vcJsonString = """
              {
                "renderMethod": { "type": "invalid", "renderSuite": "svg-mustache" }
            }
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals(emptyList<String>(), result)
    }


    @Test
    fun `renderSvg handles with renderMethod field as object - embedded SVG`() {
        val vcJsonString = """
              { 
                "credentialSubject": {
                    "fullName": "John Doe"
                },
                "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                    "template": "data:image/svg+xml;base64,PHN2Zz5GdWxsIE5hbWUgLSB7ey9jcmVkZW50aWFsU3ViamVjdC9mdWxsTmFtZX19PC9zdmc+"
                  }
              }
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals(listOf("<svg>Full Name - John Doe</svg>"), result)
    }

    @Test
    fun `renderSvg handles with renderMethod field as array - embedded multiple SVG`() {
        val vcJsonString = """
              { 
                "credentialSubject": {
                    "fullName": "John Doe",
                    "email": "test@gmail.com"
                },
                "renderMethod": [
                  {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                    "template": "data:image/svg+xml;base64,PHN2Zz5GdWxsIE5hbWUgLSB7ey9jcmVkZW50aWFsU3ViamVjdC9mdWxsTmFtZX19PC9zdmc+"
                  },
                  {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                    "template": "data:image/svg+xml;base64,PHN2Zz5FbWFpbCAtIHt7L2NyZWRlbnRpYWxTdWJqZWN0L2VtYWlsfX08L3N2Zz4="
                  }
              ]
              }
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals(listOf("<svg>Full Name - John Doe</svg>", "<svg>Email - test@gmail.com</svg>"), result)
    }

    @Test
    fun `renderSvg handles with renderMethod field as object - embedded multiple SVG with one invalid type`() {
        val vcJsonString = """
              { 
                "credentialSubject": {
                    "fullName": "John Doe",
                    "email": "test@gmail.com"
                },
                "renderMethod": [
                  {
                    "type": "invalid",
                    "renderSuite": "svg-mustache",
                    "template": "data:image/svg+xml;base64,PHN2Zz48dGV4dD5Gcm9udCBOYW1lIC0ge3tjcmVkZW50aWFsU3ViamVjdC9mdWxsTmFtZX19PC90ZXh0Pjwvc3ZnPg=="
                  },
                  {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                    "template": "data:image/svg+xml;base64,PHN2Zz5FbWFpbCAtIHt7L2NyZWRlbnRpYWxTdWJqZWN0L2VtYWlsfX08L3N2Zz4="
                  }
              ]
              }
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals(listOf("<svg>Email - test@gmail.com</svg>"), result)
    }

    @Test
    fun `renderSvg handles with renderMethod field as object - embedded multiple SVG with one invalid uri`() {
        val vcJsonString = """
              { 
                "credentialSubject": {
                    "fullName": "John Doe",
                    "email": "test@gmail.com"
                },
                "renderMethod": [
                  {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                    "template": "data:image/svg+xml;base64,@@@INVALIDBASE64###!!!"
                  },
                  {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                    "template": "data:image/svg+xml;base64,PHN2Zz5FbWFpbCAtIHt7L2NyZWRlbnRpYWxTdWJqZWN0L2VtYWlsfX08L3N2Zz4="
                  }
              ]
              }
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals(listOf("<svg>Email - test@gmail.com</svg>"), result)
    }

    @Test
    fun `renderSvg handles with renderMethod field as object - embedded multiple SVG with all invalid uri`() {
        val vcJsonString = """
              { 
                "credentialSubject": {
                    "fullName": "John Doe",
                    "email": "test@gmail.com"
                },
                "renderMethod": [
                  {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                    "template": "data:image/svg+xml;base64,@@@INVALIDBASE64###!!!"
                  },
                  {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                    "template": "data:image/svg+xml;base64,@@@INVALIDBASE64###!!!"
                  }
              ]
              }
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `renderSvg handles with renderMethod field as object - SVG Hosted`() {
        val vcJsonString = """
              { 
                "credentialSubject": {
                    "fullName": "John Doe"
                },
                "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                        "id": "https://degree.example/credential-templates/front.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                      }
                  }
              }
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals(listOf("<svg>Full Name - John Doe</svg>"), result)
    }

    @Test
    fun `renderSvg handles with renderMethod field as array - Multiple SVG Hosted`() {
        val vcJsonString = """
              { 
                "credentialSubject": {
                    "fullName": "John Doe",
                    "email": "test@gmail.com"
                },
                "renderMethod": [
                  {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                    "template": {
                        "id": "https://degree.example/credential-templates/front.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                      }
                  },
                  {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                    "template": {
                        "id": "https://degree.example/credential-templates/rear.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                      }
                  }
              ]
              }
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals(listOf("<svg>Full Name - John Doe</svg>", "<svg>Email - test@gmail.com</svg>"), result)
    }

    @Test
    fun `renderSvg handles with renderMethod field as array - One SVG Hosted and Other embedded`() {
        val vcJsonString = """
              { 
                "credentialSubject": {
                    "fullName": "John Doe",
                    "email": "test@gmail.com"
                },
                "renderMethod": [
                  {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                    "template": {
                        "id": "https://degree.example/credential-templates/front.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                      }
                  },
                  {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                    "template": "data:image/svg+xml;base64,PHN2Zz5FbWFpbCAtIHt7L2NyZWRlbnRpYWxTdWJqZWN0L2VtYWlsfX08L3N2Zz4="
                  }
              ]
              }
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals(listOf("<svg>Full Name - John Doe</svg>", "<svg>Email - test@gmail.com</svg>"), result)
    }

    @Test
    fun `renderSvg with renderProperty - Hosted one SVG`() {
        val vcJsonString = """
              { 
                "issuer": "Example University",
                "validFrom": "2023-01-01",
                "credentialSubject": {
                    "fullName": "John Doe"
                },
                "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                    "template": {
                        "id": "https://degree.example/credential-templates/with-render-property.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR",
                        "renderProperty": [
                            "/issuer", "/validFrom", "/credentialSubject/degree/name"
                          ]
                      }
                  }
              }
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals(listOf("<svg>Details - -****Example University****2023-01-01***-</svg>"), result)
    }

    @Test
    fun `renderSvg with renderProperty - Hosted multiple SVG`() {
        val vcJsonString = """
              {
                  "issuer": "Example University",
                  "validFrom": "2023-01-01",
                  "address": "123 Main St",
                  "city": "Springfield",
                  "pincode": "12345",
                  "credentialSubject": {
                    "fullName": "John Doe"
                  },
                  "renderMethod": [
                    {
                      "type": "TemplateRenderMethod",
                      "renderSuite": "svg-mustache",
                      "template": {
                        "id": "https://degree.example/credential-templates/with-render-property.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR",
                        "renderProperty": [
                          "/issuer",
                          "/validFrom",
                          "/credentialSubject/degree/name"
                        ]
                      }
                    },
                    {
                      "type": "TemplateRenderMethod",
                      "renderSuite": "svg-mustache",
                      "template": {
                        "id": "https://degree.example/credential-templates/address.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR",
                        "renderProperty": [
                          "/address",
                          "/pincode"
                        ]
                      }
                    }
                  ]
                }
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals(listOf("<svg>Details - -****Example University****2023-01-01***-</svg>", "<svg>Address : 123 Main St****-****12345***</svg>"), result)
    }


    @Test
    fun `valid embedded SVG with placeholder not in VC`() {
        val vcJsonString = """
              { 
                "credentialSubject": {
                    "middleName": "John Doe"
                },
                "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                    "template": "data:image/svg+xml;base64,PHN2Zz5GdWxsIE5hbWUgLSB7ey9jcmVkZW50aWFsU3ViamVjdC9mdWxsTmFtZX19PC9zdmc+"
                  }
              }
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals(listOf("<svg>Full Name - -</svg>"), result)
    }

    @Test
    fun `valid SVG Hosted with placeholder not in VC`() {
        val vcJsonString = """
              { 
                "credentialSubject": {
                    "middleName": "John Doe"
                },
                "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                        "id": "https://degree.example/credential-templates/front.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                      }
                  }
              }
        """.trimIndent()

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals(listOf("<svg>Full Name - -</svg>"), result)
    }

//    @Test
//    fun `render Farmer SVG`() {
//
//
//        val vcJson = """
//           {
//              "@context": [
//                "https://www.w3.org/2018/credentials/v1",
//                "https://jainhitesh9998.github.io/tempfiles/farmer-credential.json",
//                "https://w3id.org/security/suites/ed25519-2020/v1"
//              ],
//              "credentialSubject": {
//                "id": "did:jwk:eyJrdHkiO",
//                "fullName": "Ramesh",
//                "farmerID": "3823333312345",
//                "gender": "Male",
//                "mobile": "9840298402",
//                "email": "ramesh@mosip.io",
//                "dob": "1980-01-24",
//                "benefits": [
//                  "Wheat",
//                  "Corn"
//                ],
//                "ownershipType": "Owner",
//                "crop": "Rice",
//                "totalLandArea": "2.5 hectares"
//              },
//              "type": [
//                "VerifiableCredential",
//                "FarmerCredential"
//              ],
//              "renderMethod": {
//                "type": "TemplateRenderMethod",
//                "renderSuite": "svg-mustache",
//                "template": {
//                  "id": "https://281230d9ab5e.ngrok-free.app/templates/farmer_front_final.svg",
//                  "mediaType": "image/svg+xml",
//                  "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
//                }
//              }
//            }
//        """.trimIndent()
//        val expected = "<svg>English Male</svg>"
//
//        val result = injivcRenderer.renderSvg(vcJson)
//
//
//        assertEquals("<svg>-</svg>", result)
//    }


}
