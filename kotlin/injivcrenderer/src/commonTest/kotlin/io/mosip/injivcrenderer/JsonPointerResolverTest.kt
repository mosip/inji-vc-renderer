package io.mosip.injivcrenderer

import io.mosip.injivcrenderer.JsonPointerResolver.replacePlaceholders
import org.json.JSONObject
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class JsonPointerResolverTest {

    @Test
    fun `replace simple object field`() {

        val svgTemplateWithLocale = "<svg >{{/credentialSubject/gender/0/value}}##{{/credentialSubject/fullName}}</svg>"

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

        val expected = "<svg >English Male##John</svg>"

        val result = replacePlaceholders(svgTemplateWithLocale, processedJson);
        assertEquals(expected, result)
    }

    @Test
    fun `replace array fields`() {

        val svgTemplateWithLocale = "<svg >{{/credentialSubject/benefits/0}}, {{/credentialSubject/benefits/1}}</svg>"

        val processedJson = JSONObject("""{
            "credentialSubject": {
                "benefits":[
                    "Item 1 is on the list",
                    "Item 2 is on the list",
                    "Item 3 is on the list"
                ],
                "fullName": "John"
            }
        }""")

        val expected = "<svg >Item 1 is on the list, Item 2 is on the list</svg>"

        val result = replacePlaceholders(svgTemplateWithLocale, processedJson);
        assertEquals(expected, result)
    }

    @Test
    fun `replace missing pointer returns dash`() {

        val svgTemplateWithLocale = "<svg >{{/credentialSubject/email}}##{{/credentialSubject/middleName}}</svg>"

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

        val expected = "<svg >-##-</svg>"

        val result = replacePlaceholders(svgTemplateWithLocale, processedJson);
        assertEquals(expected, result)
    }

    @Test
    fun `replace Fields with locale as objects`() { //take

        val svgTemplateWithLocale = "<svg>Gender: {{/credentialSubject/gender/eng}}, பாலினம் : {{/credentialSubject/gender/tam}} </svg>"

        val processedJson = JSONObject("""{
            "credentialSubject": {
                "gender": {
                    "eng": "Male",
                    "tam": "ஆண்"
                }
            }
        }""")

        val expected = "<svg>Gender: Male, பாலினம் : ஆண் </svg>"

        val result = replacePlaceholders(svgTemplateWithLocale, processedJson);
        assertEquals(expected, result)
    }

    @Test
    fun `replace Fields with locale as array of objects`() {

        val svgTemplateWithLocale = "<svg>Gender: {{/credentialSubject/gender/0/value}}, பாலினம் : {{/credentialSubject/gender/1/value}} </svg>"

        val processedJson = JSONObject("""{
            "credentialSubject": {
                "gender": [
                    {
                        "language": "eng",
                        "value": "Male" 
                    },
                    {
                        "language": "tam",
                        "value": "ஆண்"
                    }
                ]
            }
        }""")

        val expected = "<svg>Gender: Male, பாலினம் : ஆண் </svg>"

        val result = replacePlaceholders(svgTemplateWithLocale, processedJson);
        assertEquals(expected, result)
    }

    @Test
    fun `replace nested address fields`() {

        val svgTemplateWithLocale = "<svg>{{/credentialSubject/address/addressLine1/0/value}}</svg>"

        val processedJson = JSONObject("""{
            "credentialSubject": {
                "address": {
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
                    ]
                    
                }
            }
        }""")

        val expected = "<svg>TEST_ADDRESS_LINE_1eng</svg>"

        val result = replacePlaceholders(svgTemplateWithLocale, processedJson);
        assertEquals(expected, result)
    }

    @Test
    fun `replace field with slash`() {

        val svgTemplateWithLocale = "<svg >{{/credentialSubject/ac~1dc}}</svg>"

        val processedJson = JSONObject("""{
            "credentialSubject": {
                "ac/dc": "current unit"
            }
        }""")

        val expected = "<svg >current unit</svg>"

        val result = replacePlaceholders(svgTemplateWithLocale, processedJson);
        assertEquals(expected, result)
    }

    @Test
    fun `replace field with tilde`() {

        val svgTemplateWithLocale = "<svg >{{/credentialSubject/a~0b}}</svg>"

        val processedJson = JSONObject("""{
            "credentialSubject": {
                "a~b": "test"
            }
        }""")

        val expected = "<svg >test</svg>"

        val result = replacePlaceholders(svgTemplateWithLocale, processedJson);
        assertEquals(expected, result)
    }

    @Test
    fun `pointer to root returns full document`() {
        val svgTemplate = "<svg>{{}}</svg>"
        val json = JSONObject("""{"a":1,"b":2}""")
        val expected = "<svg>${json.toString()}</svg>"

        val result = replacePlaceholders(svgTemplate, json)
        assertEquals(expected, result)
    }

    @Test
    fun `empty array and object pointers`() {
        val svgTemplate = "<svg>{{/emptyArray}},{{/emptyObject}}</svg>"
        val json = JSONObject("""{"emptyArray": [], "emptyObject": {}}""")
        val expected = "<svg>[],{}</svg>"

        val result = replacePlaceholders(svgTemplate, json)
        assertEquals(expected, result)
    }

    @Test
    fun `array index out of bounds returns dash`() {
        val svgTemplate = "<svg>{{/items/99}}</svg>"
        val json = JSONObject("""{"items": ["one","two"]}""")
        val expected = "<svg>-</svg>"

        val result = replacePlaceholders(svgTemplate, json)
        assertEquals(expected, result)
    }

    @Test
    fun `multiple tildes and slashes in key`() {
        val svgTemplate = "<svg>{{/a~0b~1c}}</svg>"
        val json = JSONObject("""{"a~b/c": "value"}""")
        val expected = "<svg>value</svg>"

        val result = replacePlaceholders(svgTemplate, json)
        assertEquals(expected, result)
    }

    @Test
    fun `special characters in keys`() {
        val svgTemplate = "<svg>{{/!@#\$%^&*()}}</svg>"
        val json = JSONObject("""{"!@#\$%^&*()": "special"}""")
        val expected = "<svg>special</svg>"

        val result = replacePlaceholders(svgTemplate, json)
        assertEquals(expected, result)
    }

    @Test
    fun `unicode characters in keys`() {
        val svgTemplate = "<svg>{{/ключ}}</svg>"
        val json = JSONObject("""{"ключ": "значение"}""")
        val expected = "<svg>значение</svg>"

        val result = replacePlaceholders(svgTemplate, json)
        assertEquals(expected, result)
    }


    @Test
    fun `replace simple object field with renderProperty`() {

        val svgTemplateWithLocale = "<svg >{{/issuer}}##{{/credentialSubject/fullName}}</svg>"

        val processedJson = JSONObject("""{
            "issuer": "did:mosip:123456789",
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

        val expected = "<svg >did:mosip:123456789##-</svg>"

        val result = replacePlaceholders(svgTemplateWithLocale, processedJson, listOf("/issuer"));
        assertEquals(expected, result)
    }

    @Test
    fun `replace simple object field with renderProperty as array index`() {

        val svgTemplateWithLocale = "<svg >{{/credentialSubject/gender/0/value}}##{{/credentialSubject/fullName}}</svg>"

        val processedJson = JSONObject("""{
            "issuer": "did:mosip:123456789",
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

        val expected = "<svg >English Male##-</svg>"

        val result = replacePlaceholders(svgTemplateWithLocale, processedJson, listOf("/credentialSubject/gender/0/value"));
        assertEquals(expected, result)
    }



}