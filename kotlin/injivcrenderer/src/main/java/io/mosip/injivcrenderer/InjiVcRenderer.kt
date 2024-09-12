package io.mosip.injivcrenderer
import android.graphics.Bitmap
import android.util.Base64
import io.mosip.injivcrenderer.Utils.fetchSvgAsText
import io.mosip.pixelpass.PixelPass
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream


class InjiVcRenderer {

    fun getValueFromData(key: String, jsonObject: JSONObject): Any? {
        val keys = key.split("/")
        var currentValue: Any? = jsonObject

        for (k in keys) {
            when (currentValue) {
                is JSONObject -> currentValue = currentValue.opt(k)
                is JSONArray -> {
                    val index = k.toIntOrNull()
                    currentValue = if (index != null && index < currentValue.length()) {
                        currentValue.opt(index)
                    } else {
                        null
                    }
                }
                else -> return null
            }
        }
        return currentValue
    }

    fun renderSvg(vcJsonData: String): String {
        try {
            val jsonObject = JSONObject(vcJsonData)
            val renderMethodArray = jsonObject.getJSONArray("renderMethod")
            val firstRenderMethod = renderMethodArray.getJSONObject(0)
            val svgUrl = firstRenderMethod.getString("id")

            var svgTemplate = fetchSvgAsText(svgUrl)
            svgTemplate = replaceQRCode(vcJsonData, svgTemplate)

            svgTemplate = replaceBenefits(jsonObject, svgTemplate)
            svgTemplate = replaceAddress(jsonObject, svgTemplate)


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
                .joinToString(",")
            val benefitsPlaceholderList = listOf(BENEFITS_PLACEHOLDER_1, BENEFITS_PLACEHOLDER_2)
            val replacedSvgWithBenefits = wrapBasedOnCharacterLength(svgTemplate, benefitsString, 55, benefitsPlaceholderList)

            return replacedSvgWithBenefits
        } catch (e: Exception){
            e.printStackTrace()
            return svgTemplate
        }
    }

    private fun replaceAddress(jsonObject: JSONObject, svgTemplate: String): String {
        try {
            val credentialSubject = jsonObject.getJSONObject("credentialSubject")
            val fields = listOf(ADDRESS_LINE_1, ADDRESS_LINE_2, ADDRESS_LINE_3, CITY, PROVINCE, REGION, POSTAL_CODE)
            val values = mutableListOf<String>()

            for (field in fields) {
                if (credentialSubject.has(field)) {
                    val array = credentialSubject.getJSONArray(field)
                    if (array.length() > 0) {
                        val value = array.getJSONObject(0).optString("value", "").trim()
                        if (value.isNotEmpty()) {
                            values.add(value)
                        }
                    }
                }
            }
            val fullAddress = values.joinToString(separator = ",")
            val addressPlacholderList = listOf(FULL_ADDRESS_PLACEHOLDER_1, FULL_ADDRESS_PLACEHOLDER_2)
            val replacedSvgWithFullAddress = wrapBasedOnCharacterLength(svgTemplate, fullAddress, 55, addressPlacholderList)
            return replacedSvgWithFullAddress
        } catch (e: Exception) {
            e.printStackTrace()
            return svgTemplate
        }
    }

    private fun wrapBasedOnCharacterLength(svgTemplate: String,
                                             dataToSplit: String,
                                             maxLength: Int,
                                             placeholdersList: List<String>): String{
        try {
            val segments = dataToSplit.chunked(maxLength).take(2)
            var replacedSvg = svgTemplate
            placeholdersList.forEachIndexed { index, placeholder ->
                if (index < segments.size) {
                    replacedSvg = replacedSvg.replaceFirst(placeholder, segments[index])
                }
            }
            return replacedSvg
        } catch (e: Exception){
            e.printStackTrace()
            return svgTemplate
        }
    }

    companion object{
        const val BASE64_IMAGE_TYPE= "data:image/png;base64,"
        const val QR_CODE_PLACEHOLDER="{{qrCodeImage}}"
        const val FULL_ADDRESS_PLACEHOLDER_1="{{fullAddress1}}"
        const val FULL_ADDRESS_PLACEHOLDER_2="{{fullAddress2}}"
        const val BENEFITS_PLACEHOLDER_1 = "{{benefits1}}"
        const val BENEFITS_PLACEHOLDER_2 = "{{benefits2}}"
        const val PLACEHOLDER_REGEX_PATTERN = "\\{\\{([^}]+)\\}\\}"


        const val ADDRESS_LINE_1 = "addressLine1"
        const val ADDRESS_LINE_2 = "addressLine2"
        const val ADDRESS_LINE_3 = "addressLine3"
        const val CITY = "city"
        const val PROVINCE = "province"
        const val REGION = "region"
        const val POSTAL_CODE = "postalCode"


    }
}