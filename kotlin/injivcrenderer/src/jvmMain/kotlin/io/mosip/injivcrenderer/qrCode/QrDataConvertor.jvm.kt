package io.mosip.injivcrenderer.qrCode

import io.mosip.pixelpass.PixelPass
import io.mosip.pixelpass.convertQRDataIntoBase64
import io.mosip.pixelpass.types.ECC


actual fun generateQrFromVcJson(vcJson: String): String {
    val pixelPass = PixelPass()
    return pixelPass.generateQRCode(vcJson)
}

actual fun generateQrFromQrData(qrData: String): String {
    return convertQRDataIntoBase64(qrData, ECC.L)
}