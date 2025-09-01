package io.mosip.injivcrenderer

import android.util.Log
import io.mosip.pixelpass.PixelPass


actual fun convertQrDataIntoBase64(qrData: String): String {
    try {
        val pixelPass = PixelPass()
        return pixelPass.generateQRCode(qrData)
    } catch (e: Exception){
        Log.d("Error occurred while converting Qr Data to Base64 String::", e.toString())
        e.printStackTrace()
        return ""
    }
}