package io.mosip.injivcrenderer

import io.mosip.injivcrenderer.constants.Constants.SHA_256
import io.mosip.injivcrenderer.exceptions.VcRendererExceptions
import io.mosip.injivcrenderer.utils.Utils
import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
class DigestMultibaseHelper(private val traceabilityId: String) {

    private val className = Utils::class.simpleName

    fun verifyDigestMultibase(svgString: String, digestMultibase: String): Boolean {
        if (!digestMultibase.startsWith("u")) throw VcRendererExceptions.MultibaseVerificationException(traceabilityId, className, "digestMultibase must start with 'u'")
        val encodedPart = digestMultibase.substring(1)

        val decoded = base64UrlNoPadDecode(encodedPart)
        if (decoded.size != 34)
            throw VcRendererExceptions.MultibaseVerificationException(traceabilityId, className, "Invalid multihash length")
        if (decoded[0] != 0x12.toByte() || decoded[1] != 0x20.toByte())
            throw VcRendererExceptions.MultibaseVerificationException(traceabilityId, className, "Unsupported multihash prefix")

        val expectedHash = decoded.copyOfRange(2, 34)
        val actualHash = MessageDigest.getInstance(SHA_256).digest(svgString.toByteArray(Charsets.UTF_8))

        return actualHash.contentEquals(expectedHash)
    }


    fun generateDigestMultibase(svgString: String): String {
        val svgBytes = svgString.toByteArray(Charsets.UTF_8)
        val hash = MessageDigest.getInstance(SHA_256).digest(svgBytes)
        val multihash = byteArrayOf(0x12, 0x20) + hash
        return "u" + base64UrlNoPadEncode(multihash)
    }


    private fun base64UrlNoPadEncode(input: ByteArray): String {
        val encoded = Base64.encode(input)
        return encoded.replace('+', '-')
            .replace('/', '_')
            .replace("=", "")
    }

    private fun base64UrlNoPadDecode(input: String): ByteArray {
        val standardBase64 = input
            .replace('-', '+')
            .replace('_', '/')
            .padEnd(input.length + (4 - input.length % 4) % 4, '=')
        return Base64.decode(standardBase64)
    }
}

