package io.mosip.injivcrenderer.qrCode

import io.mosip.pixelpass.PixelPass


actual fun convertQrDataIntoBase64(qrData: String): String {
    val pixelPass = PixelPass()
    return pixelPass.generateQRCode(qrData)
}