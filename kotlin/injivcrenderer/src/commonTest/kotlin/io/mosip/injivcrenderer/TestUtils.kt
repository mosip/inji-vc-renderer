package io.mosip.injivcrenderer

import io.mosip.injivcrenderer.constants.Constants.SHA_256
import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class TestUtils {
    fun generateDigestMultibase(svgString: String): String {
        val svgBytes = svgString.toByteArray(Charsets.UTF_8)
        val hash = MessageDigest.getInstance(SHA_256).digest(svgBytes)
        val multihash = byteArrayOf(0x12, 0x20) + hash
        return "u" + base64UrlNoPadEncode(multihash)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun base64UrlNoPadEncode(input: ByteArray): String {
        val encoded = Base64.encode(input)
        return encoded.replace('+', '-')
            .replace('/', '_')
            .replace("=", "")
    }
}