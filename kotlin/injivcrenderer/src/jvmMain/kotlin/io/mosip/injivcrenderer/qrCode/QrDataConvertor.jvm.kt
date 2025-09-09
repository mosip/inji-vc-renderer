package io.mosip.injivcrenderer.qrCode

import io.mosip.pixelpass.PixelPass


actual fun convertQrDataIntoBase64(qrData: String): String {
    try {
        val pixelPass = PixelPass()
        return pixelPass.generateQRCode(qrData)
    }
    catch (e: Exception){
        println("Error occurred while converting Qr Data to Base64 String::$e")
        e.printStackTrace()
        return ""
    }
}