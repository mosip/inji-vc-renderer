package io.mosip.injivcrenderer.qrCode

// For VC JSON string input using PixelPass.generateQRCode()
expect fun generateQrCodeFromVcJson(vcJson: String): String

// For QR payload string using convertQRDataIntoBase64() from PixelPass
expect fun generateQrCodeFromQrData(qrData: String): String