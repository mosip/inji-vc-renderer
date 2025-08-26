package io.mosip.injivcrenderer

import java.util.Base64

actual fun decodeSvgDataUriToSvgTemplate(svgDataUri: String): String? {
    val base64Part = svgDataUri.substringAfter("base64,", svgDataUri)

    return try {
        val decodedBytes: ByteArray = Base64.getDecoder().decode(base64Part)
        decodedBytes.toString(Charsets.UTF_8)
    } catch (e: Exception) {
        null
    }
}