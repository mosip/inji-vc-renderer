package io.mosip.injivcrenderer

import io.mosip.injivcrenderer.constants.Constants.QR_CODE_FALLBACK_IMAGE_ID
import io.mosip.injivcrenderer.constants.Constants.SVG_MUSTACHE
import io.mosip.injivcrenderer.constants.Constants.TEMPLATE_RENDER_METHOD
import io.mosip.injivcrenderer.constants.ContentType
import io.mosip.injivcrenderer.constants.CredentialFormat
import io.mosip.injivcrenderer.constants.VcRendererErrorCodes
import io.mosip.injivcrenderer.constants.VcRendererErrorCodes.SVG_FETCH_ERROR
import io.mosip.injivcrenderer.exceptions.VcRendererExceptions
import io.mosip.injivcrenderer.networkManager.NetworkManager
import io.mosip.injivcrenderer.networkManager.TemplateResponse
import io.mosip.injivcrenderer.qrCode.QrCodeGenerator
import io.mosip.injivcrenderer.qrCode.QrCodeGenerator.Companion.DEFAULT_FALLBACK_QR_BASE64
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.mockito.Mockito.mockConstruction
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import kotlin.test.*

@RunWith(RobolectricTestRunner::class)
class InjiVcRendererTest {

    private lateinit var injivcRenderer: InjiVcRenderer

    private lateinit var mockConstruction: AutoCloseable


    @BeforeTest
    fun setup() {
        mockConstruction = mockConstruction(NetworkManager::class.java) { mock, _ ->
            whenever(mock.fetch(any())).thenAnswer { invocation ->
                val url = invocation.arguments[0] as String
                when {
                    url.contains("empty-response") -> TemplateResponse(ContentType.SVG, "")
                    url.contains("normal.svg") -> TemplateResponse(ContentType.SVG, "<svg>Email: {{/credentialSubject/email}}, Mobile: {{/credentialSubject/mobile}}</svg>")
                    url.contains("arrays.svg") -> TemplateResponse(ContentType.SVG, "<svg>Benefits: {{/credentialSubject/benefits/0}}, {{/credentialSubject/benefits/1}}</svg>")
                    url.contains("with-locale-object.svg") -> TemplateResponse(ContentType.SVG, "<svg>Full Name - {{/credentialSubject/fullName/en}},முழுப் பெயர் - {{/credentialSubject/fullName/tam}}</svg>")
                    url.contains("with-locale-as-array-of-object.svg") -> TemplateResponse(ContentType.SVG, "<svg>Full Name - {{/credentialSubject/fullName/0/value}},முழுப் பெயர் - {{/credentialSubject/fullName/1/value}}</svg>")
                    url.contains("nested-object.svg") -> TemplateResponse(ContentType.SVG, "<svg>Address : {{/credentialSubject/addressLine1/0/value}}****{{/credentialSubject/region/0/value}}****{{/credentialSubject/city/0/value}}***</svg>")
                    url.contains("qrcode.svg") -> TemplateResponse(ContentType.SVG, "<svg>QR code : <image id = \"qrCodeImage\" xlink:href={{/qrCodeImage}}</svg>")
                    url.contains("test-digest.svg") -> TemplateResponse(ContentType.SVG, "<svg>Email: {{/credentialSubject/email}}, Mobile: {{/credentialSubject/mobile}}</svg>")
                    url.contains("xml-valid-with-2-pages.xml") -> TemplateResponse(ContentType.XML, "<pageSet>" +
                            "<page><svg>Email: {{/credentialSubject/email}}</svg></page>" +
                            "<page><svg>Mobile: {{/credentialSubject/mobile}}</svg></page>" +
                            "</pageSet>")
                    url.contains("xml-valid-with-1-page.xml") -> TemplateResponse(ContentType.XML,  "<pageSet>" +
                            "<page><svg>Email: {{/credentialSubject/email}}</svg></page>" +
                            "</pageSet>")
                    url.contains("xml-invalid-missing-svg.xml") -> TemplateResponse(ContentType.XML,  "<pageSet>" +
                            "<page>Email: {{/credentialSubject/email}}</page>" +
                            "</pageSet>")
                    url.contains("xml-invalid-missing-pageset.xml") -> TemplateResponse(ContentType.XML, "<XML></XML>")
                    url.contains("xml-invalid-missing-pages.xml") -> TemplateResponse(ContentType.XML, "<pageSet></pageSet>")
                    else -> TemplateResponse(ContentType.SVG, "<svg>default</svg>")
                }
            }
        }
        injivcRenderer = InjiVcRenderer("test-trace-id")
    }

    @AfterTest
    fun tearDown() {
        mockConstruction.close()
    }

    @Test
    fun `replace supported Format`() {

        val supportedFormat = CredentialFormat.fromValue("ldp_vc")!!
        val vcJsonString = """{
            "credentialSubject": {
                "email": "test@test.com",
                "mobile": "1234567890"
            },
            "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                        "id": "https://degree.example/credential-templates/normal.svg",
                        "mediaType": "image/svg+xml"
                      }
                  }
              }
        }"""
        val result = injivcRenderer.generateCredentialDisplayContent(credentialFormat = supportedFormat, vcJsonString = vcJsonString)
        assertEquals(
            listOf(
                "<svg>Email: test@test.com, Mobile: 1234567890</svg>"), result)

    }



    @Test
    fun `generateCredentialDisplayContent handles invalid JSON input`() {
        val vcJsonString = """{ "renderMethod": [ "invalid" ] }"""

        val actualException =
            assertFailsWith<VcRendererExceptions.InvalidRenderMethodException> {
                injivcRenderer.generateCredentialDisplayContent(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        val expectedErrorMessage = "RenderMethod object is invalid"

        assertEquals(VcRendererErrorCodes.INVALID_RENDER_METHOD, actualException.errorCode)
        assertEquals(expectedErrorMessage, actualException.message)
    }


    @Test
    fun `generateCredentialDisplayContent handles without renderMethod field`() {
        val vcJsonString = """
            {
                "someField": "someValue"
            }
        """.trimIndent()

        val actualException =
            assertFailsWith<VcRendererExceptions.InvalidRenderMethodException> {
                injivcRenderer.generateCredentialDisplayContent(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        val expectedErrorMessage = "RenderMethod object is invalid"

        assertEquals(VcRendererErrorCodes.INVALID_RENDER_METHOD, actualException.errorCode)
        assertEquals(expectedErrorMessage, actualException.message)
    }

    @Test
    fun `generateCredentialDisplayContent handles with renderMethod field as empty object`() {
        val vcJsonString = """
              { "renderMethod": {
                }
              }
        """.trimIndent()

        val actualException =
            assertFailsWith<VcRendererExceptions.InvalidRenderMethodException> {
                injivcRenderer.generateCredentialDisplayContent(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        val expectedErrorMessage = "RenderMethod object is invalid"

        assertEquals(VcRendererErrorCodes.INVALID_RENDER_METHOD, actualException.errorCode)
        assertEquals(expectedErrorMessage, actualException.message)
    }

    @Test
    fun `generateCredentialDisplayContent handles with renderMethod field as empty array`() {
        val vcJsonString = """
              {
                "renderMethod": [
                ]
            }
        """.trimIndent()

        val actualException =
            assertFailsWith<VcRendererExceptions.InvalidRenderMethodException> {
                injivcRenderer.generateCredentialDisplayContent(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        val expectedErrorMessage = "RenderMethod object is invalid"

        assertEquals(VcRendererErrorCodes.INVALID_RENDER_METHOD, actualException.errorCode)
        assertEquals(expectedErrorMessage, actualException.message)
    }



    @Test
    fun `generateCredentialDisplayContent handles with renderMethod as array - renderSuite is invalid`() {
        val vcJsonString = """
              {
                "renderMethod": [
                   { "type": "TemplateRenderMethod", "renderSuite": "invalid-suite" }
                ]
            }
        """.trimIndent()

        val actualException =
            assertFailsWith<VcRendererExceptions.InvalidRenderSuiteException> {
                injivcRenderer.generateCredentialDisplayContent(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        val expectedErrorMessage = "Render suite must be '$SVG_MUSTACHE'"

        assertEquals(VcRendererErrorCodes.INVALID_RENDER_SUITE, actualException.errorCode)
        assertEquals(expectedErrorMessage, actualException.message)
    }

    @Test
    fun `generateCredentialDisplayContent handles with renderMethod as array - type is invalid`() {
        val vcJsonString = """
              {
                "renderMethod": [
                   { "type": "invalid", "renderSuite": "svg-mustache" }
                ]
            }
        """.trimIndent()
        val actualException =
            assertFailsWith<VcRendererExceptions.InvalidRenderMethodTypeException> {
                injivcRenderer.generateCredentialDisplayContent(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        val expectedErrorMessage = "Render method type must be '$TEMPLATE_RENDER_METHOD'"

        assertEquals(VcRendererErrorCodes.INVALID_RENDER_METHOD_TYPE, actualException.errorCode)
        assertEquals(expectedErrorMessage, actualException.message)
    }

    @Test
    fun `generateCredentialDisplayContent handles with renderMethod as json - renderSuite is invalid`() {
        val vcJsonString = """
              {
                "renderMethod": { "type": "TemplateRenderMethod", "renderSuite": "invalid-suite" }
            }
        """.trimIndent()

        val actualException =
            assertFailsWith<VcRendererExceptions.InvalidRenderSuiteException> {
                injivcRenderer.generateCredentialDisplayContent(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        val expectedErrorMessage = "Render suite must be '$SVG_MUSTACHE'"

        assertEquals(VcRendererErrorCodes.INVALID_RENDER_SUITE, actualException.errorCode)
        assertEquals(expectedErrorMessage, actualException.message)
    }

    @Test
    fun `generateCredentialDisplayContent handles with renderMethod as json - type is invalid`() {
        val vcJsonString = """
              {
                "renderMethod": { "type": "invalid", "renderSuite": "svg-mustache" }
            }
        """.trimIndent()

        val actualException =
            assertFailsWith<VcRendererExceptions.InvalidRenderMethodTypeException> {
                injivcRenderer.generateCredentialDisplayContent(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        val expectedErrorMessage = "Render method type must be '$TEMPLATE_RENDER_METHOD'"

        assertEquals(VcRendererErrorCodes.INVALID_RENDER_METHOD_TYPE, actualException.errorCode)
        assertEquals(expectedErrorMessage, actualException.message)
    }





    @Test
    fun `replace address fields with locale`() {

        val vcJsonString = """{
            "credentialSubject": {
                "addressLine1": [
                    {
                        "language": "eng",
                        "value": "TEST_ADDRESS_LINE_1eng"
                    },
                   {
                        "language": "fr",
                        "value": "TEST_ADDRESS_LINE_1fr"
                    }
                ],
                "city": [
                    {
                        "language": "eng",
                        "value": "TEST_CITYeng"
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
                        "id": "https://degree.example/credential-templates/nested-object.svg",
                        "mediaType": "image/svg+xml"
                      }
                  }
              }
        }"""
        val result = injivcRenderer.generateCredentialDisplayContent(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
        assertEquals(
            listOf(
            "<svg>Address : TEST_ADDRESS_LINE_1eng****TEST_REGIONeng****TEST_CITYeng***</svg>"), result)

    }



    @Test
    fun `generateCredentialDisplayContent handles with renderMethod field as object - SVG Hosted`() {
        val vcJsonString = """
              {
                "credentialSubject": {
                    "email": "test@gmail.com",
                    "mobile": "1234567890"
                },
                "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                        "id": "https://degree.example/credential-templates/normal.svg",
                        "mediaType": "image/svg+xml"
                      }
                  }
              }
        """.trimIndent()

        val result = injivcRenderer.generateCredentialDisplayContent(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)

        assertEquals(listOf("<svg>Email: test@gmail.com, Mobile: 1234567890</svg>"), result)
    }

    @Test
    fun `generateCredentialDisplayContent handles with renderMethod field as array - Multiple SVG Hosted`() {
        val vcJsonString = """
              {
                "credentialSubject": {
                    "mobile": "John Doe",
                    "email": "test@gmail.com",
                    "fullName": {
                        "en": "John Doe",
                        "tam": "ஜான் டோ"
                    }
                },
                "renderMethod": [
                  {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                    "template": {
                        "id": "https://degree.example/credential-templates/normal.svg",
                        "mediaType": "image/svg+xml"
                      }
                  },
                  {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                    "template": {
                        "id": "https://degree.example/credential-templates/with-locale-object.svg",
                        "mediaType": "image/svg+xml"
                      }
                  }
              ]
              }
        """.trimIndent()

        val result = injivcRenderer.generateCredentialDisplayContent(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)

        assertEquals(listOf("<svg>Email: test@gmail.com, Mobile: John Doe</svg>", "<svg>Full Name - John Doe,முழுப் பெயர் - ஜான் டோ</svg>"), result)
    }

    @Test
    fun `generateCredentialDisplayContent with renderProperty - Hosted one SVG`() {
        val vcJsonString = """
              {
                "issuer": "Example University",
                "validFrom": "2023-01-01",
                "credentialSubject": {
                    "fullName": "John Doe",
                    "name": "Tester",
                    "email": "test@test.com"
                },
                "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                    "template": {
                        "id": "https://degree.example/credential-templates/normal.svg",
                        "mediaType": "image/svg+xml",
                        "renderProperty": [
                            "/issuer", "/credentialSubject/email", "/credentialSubject/degree/name"
                          ]
                      }
                  }
              }
        """.trimIndent()

        val result = injivcRenderer.generateCredentialDisplayContent(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)

        assertEquals(listOf("<svg>Email: test@test.com, Mobile: -</svg>"), result)
    }

    @Test
    fun `generateCredentialDisplayContent missing template id`() {
        val vcJsonString = """ {
                "credentialSubject": {
                    "fullName": [
                        {
                            "language": "eng",
                            "value": "John Doe"
                        },
                        {
                            "language": "tam",
                            "value": "ஜான் டோ"
                        }
                    ],
                    "mobile": "1234567890"
                },
                "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                      }
                  }
              }"""

        val actualException =
            assertFailsWith<VcRendererExceptions.MissingTemplateIdException> {
                injivcRenderer.generateCredentialDisplayContent(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        val expectedErrorMessage = "Template ID is missing in renderMethod"

        assertEquals(VcRendererErrorCodes.MISSING_TEMPLATE_ID, actualException.errorCode)
        assertEquals(expectedErrorMessage, actualException.message)
    }

    @Test
    fun `test injectQrCode`() {
        val vcJsonString = """ {
                "credentialSubject": {
                    "fullName": [
                        {
                            "language": "eng",
                            "value": "John Doe"
                        },
                        {
                            "language": "tam",
                            "value": "ஜான் டோ"
                        }
                    ],
                    "mobile": "1234567890"
                },
                "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                      "id": "https://degree.example/credential-templates/qrcode.svg",
                        "mediaType": "image/svg+xml"
                      }
                  }
              }"""

        val result = injivcRenderer.generateCredentialDisplayContent(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
        assertEquals(result.contains("{{/qrCodeImage}}"), false)

    }

    @Test
    fun `test injectQrCode when Qr data present`() {
        val vcJsonString = """ {
                "credentialSubject": {
                    "fullName": [
                        {
                            "language": "eng",
                            "value": "John Doe"
                        },
                        {
                            "language": "tam",
                            "value": "ஜான் டோ"
                        }
                    ],
                    "mobile": "1234567890"
                },
                "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                      "id": "https://degree.example/credential-templates/qrcode.svg",
                        "mediaType": "image/svg+xml"
                      }
                  }
              }"""
        val qrCodeData = "https://sample-redirection-request/redirect?vc=eyJ2YzIuMC4wIjoiLi4uIn0"

        val result = injivcRenderer.generateCredentialDisplayContent(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString, qrCodeData = qrCodeData).first() as String
        assertFalse(result.contains("{{/qrCodeImage}}"))

        assertTrue(result.contains("qrCodeImage"))
        assertFalse(result.contains("qrCodeFallbackImage"))
    }

    @Test
    fun `generateCredentialDisplayContent injects fallback QR when generation fails`() {
        mockConstruction(QrCodeGenerator::class.java) { mock, _ ->
            whenever(mock.generateFromVcJson(any())).thenThrow(RuntimeException("QR failed"))
        }.use {
            val vcJsonString = """{
          "credentialSubject": { "fullName": "John" },
          "renderMethod": {
            "type": "TemplateRenderMethod",
            "renderSuite": "svg-mustache",
            "template": {
              "id": "https://degree.example/credential-templates/qrcode.svg",
              "mediaType": "image/svg+xml"
            }
          }
        }"""

            val result = injivcRenderer.generateCredentialDisplayContent(
                CredentialFormat.LDP_VC,
                vcJsonString = vcJsonString
            ).first() as String

            assertTrue(
                result.contains("data:image/png;base64,${DEFAULT_FALLBACK_QR_BASE64}"),
                "Expected fallback QR to be injected, but was:\n$result"
            )
            assertTrue(result.contains(QR_CODE_FALLBACK_IMAGE_ID))
        }
    }

    @Test
    fun `generateCredentialDisplayContent injects fallback QR when generator returns empty string`() {
        mockConstruction(QrCodeGenerator::class.java) { mock, _ ->
            whenever(mock.generateFromVcJson(any())).thenReturn("")
        }.use {
            val vcJsonString = """{
          "credentialSubject": { "fullName": "Jane" },
          "renderMethod": {
            "type": "TemplateRenderMethod",
            "renderSuite": "svg-mustache",
            "template": {
              "id": "https://degree.example/credential-templates/qrcode.svg",
              "mediaType": "image/svg+xml"
            }
          }
        }"""

            val result = injivcRenderer.generateCredentialDisplayContent(
                CredentialFormat.LDP_VC,
                vcJsonString = vcJsonString
            ).first() as String
            assertTrue(
                result.contains("data:image/png;base64,${DEFAULT_FALLBACK_QR_BASE64}"),
                "Expected fallback QR to be injected when qrBase64 is empty, but was:\n$result"
            )
            assertTrue(result.contains(QR_CODE_FALLBACK_IMAGE_ID))
        }
    }

    @Test
    fun `digestMultibase Valid`() {

        val supportedFormat = CredentialFormat.fromValue("ldp_vc")!!
        val vcJsonString = """{
            "credentialSubject": {
                "email": "test@test.com",
                "mobile": "1234567890"
            },
            "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                        "id": "https://degree.example/credential-templates/test-digest.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "uEiCi0x0IkXhQiFxa2wdnrJL02byQYoLKjN4o9_jHxh1shw"
                      }
                  }
              }
        }"""
        val result = injivcRenderer.generateCredentialDisplayContent(credentialFormat = supportedFormat, vcJsonString = vcJsonString)
        assertEquals(
            listOf(
                "<svg>Email: test@test.com, Mobile: 1234567890</svg>"), result)

    }

    @Test
    fun `test digestMultibase Invalid`() {

        val vcJsonString = """{
            "credentialSubject": {
                "email": "test@test.com",
                "mobile": "1234567890"
            },
            "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                        "id": "https://degree.example/credential-templates/test-digest.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "uEiDc1-CXqeAP2klpU-FcUFH5etlFW2Za-aOyY221sRfcug"
                      }
                  }
              }
        }"""

        val actualException =
            assertFailsWith<VcRendererExceptions.MultibaseValidationException> {
                injivcRenderer.generateCredentialDisplayContent(CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        val expectedErrorMessage = "Mismatch between fetched SVG and provided digestMultibase"

        assertEquals(VcRendererErrorCodes.MULTIBASE_VALIDATION_FAILED, actualException.errorCode)
        assertTrue(actualException.message.contains(expectedErrorMessage))

    }

    @Test
    fun `test digestMultibase Invalid-prefix`() {

        val vcJsonString = """{
            "credentialSubject": {
                "email": "test@test.com",
                "mobile": "1234567890"
            },
            "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                        "id": "https://degree.example/credential-templates/test-digest.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zEiDc1-CXqeAP2klpU-FcUFH5etlFW2Za-aOyY221sRfcug"
                      }
                  }
              }
        }"""

        val actualException =
            assertFailsWith<VcRendererExceptions.MultibaseValidationException> {
                injivcRenderer.generateCredentialDisplayContent(CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        val expectedErrorMessage = "Multibase validation failed: digestMultibase must start with 'u'"

        assertEquals(VcRendererErrorCodes.MULTIBASE_VALIDATION_FAILED, actualException.errorCode)
        assertTrue(actualException.message.contains(expectedErrorMessage))

    }

    @Test
    fun `test valid xml - with 2 pages`() {
        val vcJsonString = """{
            "credentialSubject": {
                "email": "test@test.com",
                "mobile": "1234567890"
            },
            "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                        "id": "xml-valid-with-2-pages.xml",
                        "mediaType": "application/xml"
                      }
                  }
              }
        }"""
        val result = injivcRenderer.generateCredentialDisplayContent(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
        "<pageSet>" +
                "<page><svg>Email: {{/credentialSubject/email}}</svg></page>" +
                "<page><svg>Mobile: {{/credentialSubject/mobile}}</svg></page>" +
                "</pageSet>"
        assertEquals(
            listOf(
                "<svg>Email: test@test.com</svg>", "<svg>Mobile: 1234567890</svg>"), result)

    }

    @Test
    fun `test valid xml - with 1 page`() {
        val vcJsonString = """{
            "credentialSubject": {
                "email": "test@test.com",
                "mobile": "1234567890"
            },
            "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                        "id": "xml-valid-with-1-page.xml",
                        "mediaType": "application/xml"
                      }
                  }
              }
        }"""
        val result = injivcRenderer.generateCredentialDisplayContent(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
        "<pageSet>" +
                "<page><svg>Email: {{/credentialSubject/email}}</svg></page>" +
                "</pageSet>"
        assertEquals(
            listOf(
                "<svg>Email: test@test.com</svg>"), result)

    }

    @Test
    fun `test invalid xml - missing pageset`() {
        val vcJsonString = """{
            "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                        "id": "xml-invalid-missing-pageset.xml",
                        "mediaType": "application/xml"
                      }
                  }
              }
        }"""

        val actualException =
            assertFailsWith<VcRendererExceptions.PageSetParsingException> {
                injivcRenderer.generateCredentialDisplayContent(CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        assertEquals(VcRendererErrorCodes.XML_PARSING_FAILED, actualException.errorCode)
        assertTrue(actualException.message!!.contains("Root element must be <pageSet>"))

    }

    @Test
    fun `test invalid xml - missing page`() {
        val vcJsonString = """{
            "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                        "id": "xml-invalid-missing-pages.xml",
                        "mediaType": "application/xml"
                      }
                  }
              }
        }"""

        val actualException =
            assertFailsWith<VcRendererExceptions.PageSetParsingException> {
                injivcRenderer.generateCredentialDisplayContent(CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        assertEquals(VcRendererErrorCodes.XML_PARSING_FAILED, actualException.errorCode)
        assertTrue(actualException.message!!.contains("<pageSet> must contain at least one <page> element"))

    }

    @Test
    fun `test invalid xml - missing svg`() {
        val vcJsonString = """{
            "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                        "id": "xml-invalid-missing-svg.xml",
                        "mediaType": "application/xml"
                      }
                  }
              }
        }"""

        val actualException =
            assertFailsWith<VcRendererExceptions.PageSetParsingException> {
                injivcRenderer.generateCredentialDisplayContent(CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        assertEquals(VcRendererErrorCodes.XML_PARSING_FAILED, actualException.errorCode)
        assertTrue(actualException.message!!.contains("does not contain a <svg> element"))

    }

    @Test
    fun `test empty response - from network call`() {
        val vcJsonString = """{
            "renderMethod": {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                      "template": {
                        "id": "empty-response.svg",
                        "mediaType": "image/svg+xml"
                      }
                  }
              }
        }"""

        val actualException =
            assertFailsWith<VcRendererExceptions.SvgFetchException> {
                injivcRenderer.generateCredentialDisplayContent(CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        kotlin.test.assertEquals(SVG_FETCH_ERROR, actualException.errorCode)
        assert(actualException.message!!.contains("Empty response body"))

    }

}

