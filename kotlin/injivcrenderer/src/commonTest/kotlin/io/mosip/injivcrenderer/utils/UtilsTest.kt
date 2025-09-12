package io.mosip.injivcrenderer.utils

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import io.mosip.injivcrenderer.TestUtils
import io.mosip.injivcrenderer.exceptions.VcRendererExceptions

class UtilsTest {

    private val traceId = "test-trace-id"
    private val helper = Utils(traceId)
    private val svgSample = """<svg>Email: {{/credentialSubject/email}}, Mobile: {{/credentialSubject/mobile}}</svg>"""

    @Test
    fun `generateDigestMultibase should produce a string starting with u`() {
        val digest = TestUtils().generateDigestMultibase(svgSample)
        println("digest: $digest")
        assertTrue(digest.startsWith("u"), "Digest must start with 'u'")
    }

    @Test
    fun `validateDigestMultibase should return true for correct digest`() {
        val digest = "uEiCi0x0IkXhQiFxa2wdnrJL02byQYoLKjN4o9_jHxh1shw"
        val result = helper.validateDigestMultibase(svgSample, digest)
        assertTrue(result, "Verification should succeed for correct digest")
    }

    @Test
    fun `validateDigestMultibase should return false for incorrect digest`() {
        val wrongDigest = "uEiDc1-CXqeAP2klpU-FcUFH5etlFW2Za-aOyY221sRfcug"
        val result = helper.validateDigestMultibase(svgSample, wrongDigest)
        assertFalse(result, "Verification should fail for incorrect digest")
    }

    @Test
    fun `validateDigestMultibase should throw exception when digest does not start with u`() {

        assertFailsWith<VcRendererExceptions.MultibaseValidationException> {
            helper.validateDigestMultibase(svgSample, "xInvalidDigest")
        }
    }

    @Test
    fun `validateDigestMultibase should throw exception for invalid multihash length`() {
        val invalidDigest = "uAA"
        assertFailsWith<VcRendererExceptions.MultibaseValidationException> {
            helper.validateDigestMultibase(svgSample, invalidDigest)
        }
    }

    @Test
    fun `validateDigestMultibase should throw exception for wrong prefix bytes`() {
        val digest = TestUtils().generateDigestMultibase(svgSample)
        val corrupted = "u" + digest.substring(1).replaceFirst(digest[1], 'A')
        assertFailsWith<VcRendererExceptions.MultibaseValidationException> {
            helper.validateDigestMultibase(svgSample, corrupted)
        }
    }
}
