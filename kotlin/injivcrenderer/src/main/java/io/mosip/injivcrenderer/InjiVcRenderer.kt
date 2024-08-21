package io.mosip.injivcrenderer
import android.graphics.Bitmap
import android.util.Base64
import io.mosip.injivcrenderer.Utils.fetchSvgAsText
import io.mosip.pixelpass.PixelPass
import org.json.JSONObject
import java.io.ByteArrayOutputStream


class InjiVcRenderer {

    fun getValueFromData(key: String, data: JSONObject): Any? {
        val keys = key.split("/")
        var value: Any? = data
        for (k in keys) {
            if (value is JSONObject) {
                value = value.opt(k)
            } else {
                return null
            }
        }
        return value
    }

    fun renderSvg(vcJsonData: String): String {
        try {
            val jsonObject = JSONObject(vcJsonData)
            val renderMethodArray = jsonObject.getJSONArray("renderMethod")
            val firstRenderMethod = renderMethodArray.getJSONObject(0)
            val svgUrl = firstRenderMethod.getString("id")

            var svgTemplate = fetchSvgAsText(svgUrl)

            svgTemplate = replaceBenefits(jsonObject, svgTemplate)
            svgTemplate = replaceQRCode(vcJsonData, svgTemplate)

            val regex = Regex(PLACEHOLDER_REGEX_PATTERN)
            var result = regex.replace(svgTemplate) { match ->
                val key = match.groups[1]?.value?.trim() ?: ""
                val value = getValueFromData(key, jsonObject)
                value?.toString() ?: ""
            }
            return result
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    private fun replaceQRCode(vcJson: String, svgTemplate: String): String {
        try {
            val pixelPass = PixelPass()
            val qrCode: Bitmap = pixelPass.generateQRCode(vcJson)
            val byteArrayOutputStream = ByteArrayOutputStream()
            qrCode.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val base64String: String = Base64.encodeToString(byteArray, Base64.DEFAULT)
            if (base64String.isNotEmpty()) {
                return svgTemplate.replace(QR_CODE_PLACEHOLDER, "$BASE64_IMAGE_TYPE$base64String");
            }
            return svgTemplate;
        } catch (e: Exception){
            e.printStackTrace()
            return svgTemplate;
        }

    }

    private fun replaceBenefits(jsonObject: JSONObject, svgTemplate: String): String {
        try {
            val credentialSubject = jsonObject.getJSONObject("credentialSubject")
            val benefitsArray = credentialSubject.getJSONArray("benefits")
            val benefitsString = (0 until benefitsArray.length())
                .map { benefitsArray.getString(it) }
                .joinToString(", ")
            return svgTemplate.replace(BENEFITS_PLACEHOLDER, benefitsString);
        } catch (e: Exception){
            e.printStackTrace()
            return svgTemplate
        }
    }

    companion object{
        const val BASE64_IMAGE_TYPE= "data:image/png;base64,"
        const val QR_CODE_PLACEHOLDER="{{qrCodeImage}}"
        const val BENEFITS_PLACEHOLDER = "{{credentialSubject/benefits}}"
        const val PLACEHOLDER_REGEX_PATTERN = "\\{\\{(.*?)\\}\\}"

    }
}
