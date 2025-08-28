package io.mosip.injivcrenderer

import io.mosip.pixelpass.PixelPass

class QrCodeGenerator {
    fun generateQRCodeImage(vcJson: String): String {
        try {
            val pixelPass = PixelPass()
            val qrData: String = pixelPass.generateQRData(vcJson)

            if (qrData.length <= 10000) {
                return BASE64_PNG_IMAGE_PREFIX +convertQrDataIntoBase64(qrData)
            }
            return ""
        } catch (e: Exception){
            e.printStackTrace()
            return "";
        }

    }

    companion object {
        const val BASE64_PNG_IMAGE_PREFIX= "data:image/png;base64,"
    }
}