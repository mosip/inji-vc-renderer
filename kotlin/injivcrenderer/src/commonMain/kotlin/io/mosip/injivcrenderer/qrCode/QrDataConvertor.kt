package io.mosip.injivcrenderer.qrCode

// For VC JSON string input using PixelPass.generateQRCode()
expect fun generateQrFromVcJson(vcJson: String): String

// For QR payload string using convertQRDataIntoBase64() from PixelPass
expect fun generateQrFromQrData(qrData: String): String