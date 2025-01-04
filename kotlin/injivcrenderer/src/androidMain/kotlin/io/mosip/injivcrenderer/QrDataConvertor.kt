package io.mosip.injivcrenderer

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream


actual fun convertQrDataIntoBase64(qrData: String): String {
    try {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix: BitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 650, 650)
        val bitmap = toBitmap(bitMatrix)
        return encodeToString(bitmap).orEmpty()
    } catch (e: Exception){
        Log.d("Error occurred while converting Qr Data to Base64 String::", e.toString())
        e.printStackTrace()
        return ""
    }
}


private fun toBitmap(bitMatrix: BitMatrix): Bitmap {
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}

private fun encodeToString(image: Bitmap): String? {
    val imageString: String?
    val bos = ByteArrayOutputStream()
    image.compress(Bitmap.CompressFormat.PNG, 100, bos)
    val imageBytes = bos.toByteArray()
    imageString = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    bos.close()
    return imageString
}