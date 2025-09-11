package io.mosip.injivcrenderer

import io.mosip.injivcrenderer.constants.Constants.SVG_MUSTACHE
import io.mosip.injivcrenderer.constants.Constants.TEMPLATE_RENDER_METHOD
import io.mosip.injivcrenderer.constants.CredentialFormat
import io.mosip.injivcrenderer.constants.VcRendererErrorCodes
import io.mosip.injivcrenderer.exceptions.VcRendererExceptions
import io.mosip.injivcrenderer.networkManager.NetworkManager
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
            whenever(mock.fetchSvgAsText(any())).thenAnswer { invocation ->
                val url = invocation.arguments[0] as String
                when {
                    url.contains("normal.svg") -> "<svg>Email: {{/credentialSubject/email}}, Mobile: {{/credentialSubject/mobile}}</svg>"
                    url.contains("arrays.svg") -> "<svg>Benefits: {{/credentialSubject/benefits/0}}, {{/credentialSubject/benefits/1}}</svg>"
                    url.contains("with-locale-object.svg") -> "<svg>Full Name - {{/credentialSubject/fullName/en}},முழுப் பெயர் - {{/credentialSubject/fullName/tam}}</svg>"
                    url.contains("with-locale-as-array-of-object.svg") -> "<svg>Full Name - {{/credentialSubject/fullName/0/value}},முழுப் பெயர் - {{/credentialSubject/fullName/1/value}}</svg>"
                    url.contains("nested-object.svg") -> "<svg>Address : {{/credentialSubject/addressLine1/0/value}}****{{/credentialSubject/region/0/value}}****{{/credentialSubject/city/0/value}}***</svg>"
                    url.contains("qrcode.svg") -> "<svg>QR code : <image id = \"qrCodeImage\" xlink:href{{/qrCodeImage}}</svg>"
                    url.contains("multilingual.svg") -> "<svg>" +
                            "{{/credential_definition/credentialSubject/fullName/display/0/name}}: {{/credentialSubject/fullName/0/value}}," +
                            "{{/credential_definition/credentialSubject/fullName/display/1/name}}: {{/credentialSubject/fullName/1/value}}" +
                            "</svg>"
                    else -> "<svg>default</svg>"
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
    fun `renderVC should throw UnsupportedCredentialFormat when format is not LDP_VC`() {
        val unsupportedFormat = CredentialFormat.fromValue("mso_mdoc")

        val vcJson = """
            {
              "credentialSubject": {
                "fullName": "John Doe"
              }
            }
        """.trimIndent()

        val actualException =
            assertFailsWith<VcRendererExceptions.UnsupportedCredentialFormat> {
                injivcRenderer.renderVC(unsupportedFormat, vcJsonString = vcJson)
            }
        val expectedErrorMessage = "Only LDP_VC credential format is supported"

        assertEquals(VcRendererErrorCodes.UNSUPPORTED_CREDENTIAL_FORMAT, actualException.errorCode)
        assertEquals(expectedErrorMessage, actualException.message)
    }

    @Test
    fun `replace supported Format`() {

        val supportedFormat = CredentialFormat.fromValue("ldp_vc")
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
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                      }
                  }
              }
        }"""
        val result = injivcRenderer.renderVC(credentialFormat = supportedFormat, vcJsonString = vcJsonString)
        assertEquals(
            listOf(
                "<svg>Email: test@test.com, Mobile: 1234567890</svg>"), result)

    }



    @Test
    fun `renderVC handles invalid JSON input`() {
        val vcJsonString = """{ "renderMethod": [ "invalid" ] }"""

        val actualException =
            assertFailsWith<VcRendererExceptions.InvalidRenderMethodException> {
                injivcRenderer.renderVC(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        val expectedErrorMessage = "RenderMethod object is invalid"

        assertEquals(VcRendererErrorCodes.INVALID_RENDER_METHOD, actualException.errorCode)
        assertEquals(expectedErrorMessage, actualException.message)
    }


    @Test
    fun `renderVC handles without renderMethod field`() {
        val vcJsonString = """
            {
                "someField": "someValue"
            }
        """.trimIndent()

        val actualException =
            assertFailsWith<VcRendererExceptions.InvalidRenderMethodException> {
                injivcRenderer.renderVC(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        val expectedErrorMessage = "RenderMethod object is invalid"

        assertEquals(VcRendererErrorCodes.INVALID_RENDER_METHOD, actualException.errorCode)
        assertEquals(expectedErrorMessage, actualException.message)
    }

    @Test
    fun `renderVC handles with renderMethod field as empty object`() {
        val vcJsonString = """
              { "renderMethod": {
                }
              }
        """.trimIndent()

        val actualException =
            assertFailsWith<VcRendererExceptions.InvalidRenderMethodException> {
                injivcRenderer.renderVC(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        val expectedErrorMessage = "RenderMethod object is invalid"

        assertEquals(VcRendererErrorCodes.INVALID_RENDER_METHOD, actualException.errorCode)
        assertEquals(expectedErrorMessage, actualException.message)
    }

    @Test
    fun `renderVC handles with renderMethod field as empty array`() {
        val vcJsonString = """
              {
                "renderMethod": [
                ]
            }
        """.trimIndent()

        val actualException =
            assertFailsWith<VcRendererExceptions.InvalidRenderMethodException> {
                injivcRenderer.renderVC(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        val expectedErrorMessage = "RenderMethod object is invalid"

        assertEquals(VcRendererErrorCodes.INVALID_RENDER_METHOD, actualException.errorCode)
        assertEquals(expectedErrorMessage, actualException.message)
    }



    @Test
    fun `renderVC handles with renderMethod as array - renderSuite is invalid`() {
        val vcJsonString = """
              {
                "renderMethod": [
                   { "type": "TemplateRenderMethod", "renderSuite": "invalid-suite" }
                ]
            }
        """.trimIndent()

        val actualException =
            assertFailsWith<VcRendererExceptions.InvalidRenderSuiteException> {
                injivcRenderer.renderVC(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        val expectedErrorMessage = "Render suite must be '$SVG_MUSTACHE'"

        assertEquals(VcRendererErrorCodes.INVALID_RENDER_SUITE, actualException.errorCode)
        assertEquals(expectedErrorMessage, actualException.message)
    }

    @Test
    fun `renderVC handles with renderMethod as array - type is invalid`() {
        val vcJsonString = """
              {
                "renderMethod": [
                   { "type": "invalid", "renderSuite": "svg-mustache" }
                ]
            }
        """.trimIndent()
        val actualException =
            assertFailsWith<VcRendererExceptions.InvalidRenderMethodTypeException> {
                injivcRenderer.renderVC(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        val expectedErrorMessage = "Render method type must be '$TEMPLATE_RENDER_METHOD'"

        assertEquals(VcRendererErrorCodes.INVALID_RENDER_METHOD_TYPE, actualException.errorCode)
        assertEquals(expectedErrorMessage, actualException.message)
    }

    @Test
    fun `renderVC handles with renderMethod as json - renderSuite is invalid`() {
        val vcJsonString = """
              {
                "renderMethod": { "type": "TemplateRenderMethod", "renderSuite": "invalid-suite" }
            }
        """.trimIndent()

        val actualException =
            assertFailsWith<VcRendererExceptions.InvalidRenderSuiteException> {
                injivcRenderer.renderVC(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
            }
        val expectedErrorMessage = "Render suite must be '$SVG_MUSTACHE'"

        assertEquals(VcRendererErrorCodes.INVALID_RENDER_SUITE, actualException.errorCode)
        assertEquals(expectedErrorMessage, actualException.message)
    }

    @Test
    fun `renderVC handles with renderMethod as json - type is invalid`() {
        val vcJsonString = """
              {
                "renderMethod": { "type": "invalid", "renderSuite": "svg-mustache" }
            }
        """.trimIndent()

        val actualException =
            assertFailsWith<VcRendererExceptions.InvalidRenderMethodTypeException> {
                injivcRenderer.renderVC(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
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
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                      }
                  }
              }
        }"""
        val result = injivcRenderer.renderVC(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
        assertEquals(
            listOf(
            "<svg>Address : TEST_ADDRESS_LINE_1eng****TEST_REGIONeng****TEST_CITYeng***</svg>"), result)

    }



    @Test
    fun `renderVC handles with renderMethod field as object - SVG Hosted`() {
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
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                      }
                  }
              }
        """.trimIndent()

        val result = injivcRenderer.renderVC(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)

        assertEquals(listOf("<svg>Email: test@gmail.com, Mobile: 1234567890</svg>"), result)
    }

    @Test
    fun `renderVC handles with renderMethod field as array - Multiple SVG Hosted`() {
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
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                      }
                  },
                  {
                    "type": "TemplateRenderMethod",
                    "renderSuite": "svg-mustache",
                    "template": {
                        "id": "https://degree.example/credential-templates/with-locale-object.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                      }
                  }
              ]
              }
        """.trimIndent()

        val result = injivcRenderer.renderVC(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)

        assertEquals(listOf("<svg>Email: test@gmail.com, Mobile: John Doe</svg>", "<svg>Full Name - John Doe,முழுப் பெயர் - ஜான் டோ</svg>"), result)
    }

    @Test
    fun `renderVC with renderProperty - Hosted one SVG`() {
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
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR",
                        "renderProperty": [
                            "/issuer", "/credentialSubject/email", "/credentialSubject/degree/name"
                          ]
                      }
                  }
              }
        """.trimIndent()

        val result = injivcRenderer.renderVC(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)

        assertEquals(listOf("<svg>Email: test@test.com, Mobile: -</svg>"), result)
    }

    @Test
    fun `renderVC with wellKnown and label placeholder present in svg`() {
        val vcJsonString = """
              {
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
                        "id": "https://degree.example/credential-templates/multilingual.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                      }
                  }
              }
        """.trimIndent()

        val wellKnownJsonString = """
              {
                "credential_definition": {
                  "type": [
                    "FarmerCredential_WithFace",
                    "VerifiableCredential"
                  ],
                  "credentialSubject": {
                    "fullName": {
                          "display": [
                             {
                                "language": "eng",
                                "name": "Full Name"
                            },
                            {
                                "language": "tam",
                                "name": "முழுப் பெயர்"
                            }
                          ]
                    }
                  }
                }
              }
        """.trimIndent()

        val result = injivcRenderer.renderVC(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString, wellKnownJson = wellKnownJsonString)
        assertEquals(listOf("<svg>" +
                "Full Name: John Doe," +
                "முழுப் பெயர்: ஜான் டோ" +
                "</svg>"), result)
    }

    @Test
    fun `renderVC without wellKnown and label placeholder present in svg (fallback)`() {
        val vcJsonString = """
              {
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
                        "id": "https://degree.example/credential-templates/multilingual.svg",
                        "mediaType": "image/svg+xml",
                        "digestMultibase": "zQmerWC85Wg6wFl9znFCwYxApG270iEu5h6JqWAPdhyxz2dR"
                      }
                  }
              }
        """.trimIndent()

        "<svg>" +
                "{{/credential_definition/credentialSubject/fullName/display/0/name}}: {{/credentialSubject/fullName/0/value}}," +
                "{{/credential_definition/credentialSubject/fullName/display/1/name}}: {{/credentialSubject/fullName/1/value}}" +
                "</svg>"

        val result = injivcRenderer.renderVC(credentialFormat = CredentialFormat.LDP_VC, vcJsonString = vcJsonString)
        assertEquals(listOf("<svg>" +
                "Full Name: John Doe," +
                "Full Name: ஜான் டோ" +
                "</svg>"), result)
    }



}

