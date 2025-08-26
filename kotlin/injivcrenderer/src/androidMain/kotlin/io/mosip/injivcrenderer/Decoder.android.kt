package io.mosip.injivcrenderer

import android.util.Base64

actual fun decodeSvgDataUriToSvgTemplate(svgDataUri: String): String? {

    val base64Part = svgDataUri.substringAfter("base64,", svgDataUri)

    return try {
        val decodedBytes: ByteArray = Base64.decode(base64Part, Base64.DEFAULT)
        decodedBytes.toString(Charsets.UTF_8)
    } catch (e: Exception) {
        null
    }

}