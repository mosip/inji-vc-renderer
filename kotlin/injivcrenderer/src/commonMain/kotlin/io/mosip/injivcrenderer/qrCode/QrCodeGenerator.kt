package io.mosip.injivcrenderer.qrCode

import io.mosip.injivcrenderer.constants.Constants.UNKNOWN_ERROR
import io.mosip.injivcrenderer.exceptions.VcRendererExceptions
import io.mosip.pixelpass.PixelPass


class QrCodeGenerator(private val traceabilityId: String) {
    private val className = QrCodeGenerator::class.simpleName

    fun generateQRCodeImage( vcJson: String): String {
        try {
            val pixelPass = PixelPass()
            val qrData: String = pixelPass.generateQRData(vcJson)

            return convertQrDataIntoBase64(qrData)

        } catch (e: Exception){
            throw VcRendererExceptions.QRCodeGenerationFailureException(traceabilityId, e.message ?: UNKNOWN_ERROR,  className)
        }
    }

}