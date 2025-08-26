package io.mosip.injivcrenderer

import android.util.Log
import io.mosip.pixelpass.PixelPass


actual fun convertQrDataIntoBase64(qrData: String): String {
    try {
        val pixelPass = PixelPass()
        val base64PngImage = pixelPass.generateQRCode(qrData)
        return base64PngImage
    } catch (e: Exception){
        Log.d("Error occurred while converting Qr Data to Base64 String::", e.toString())
        e.printStackTrace()
        return ""
    }
}