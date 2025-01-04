package io.mosip.injivcrenderer

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO


actual fun convertQrDataIntoBase64(qrData: String): String {
    try {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix: BitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 650, 650)
        val qrImage: BufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)
        return encodeToString(qrImage, "png").orEmpty()
    }
    catch (e: Exception){
        println("Error occurred while converting Qr Data to Base64 String::$e")
        e.printStackTrace()
        return ""
    }
}
private fun encodeToString(image: BufferedImage?, type: String?): String? {
    var imageString: String? = null
    val bos = ByteArrayOutputStream()

    try {
        ImageIO.write(image, type, bos)
        val imageBytes = bos.toByteArray()
        val encoder = Base64.getEncoder()
        imageString = encoder.encodeToString(imageBytes)
        bos.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return imageString
}