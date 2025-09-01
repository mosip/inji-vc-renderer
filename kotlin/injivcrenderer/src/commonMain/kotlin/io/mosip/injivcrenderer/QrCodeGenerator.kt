package io.mosip.injivcrenderer

import io.mosip.pixelpass.PixelPass

class QrCodeGenerator {
    fun generateQRCodeImage(vcJson: String): String {
        try {
            val pixelPass = PixelPass()
            val qrData: String = pixelPass.generateQRData(vcJson)

            return convertQrDataIntoBase64(qrData)

        } catch (e: Exception){
            e.printStackTrace()
            return "";
        }
    }

}