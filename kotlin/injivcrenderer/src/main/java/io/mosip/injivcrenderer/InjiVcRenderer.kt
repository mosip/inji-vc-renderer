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
                if(key.contains("_")){
                    val value = replaceLocaleBasedValue(key, jsonObject)
                    value ?: ""
                } else {
                    val value = getValueFromData(key, jsonObject)
                    value?.toString() ?: ""
                }

            }
            return result
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    fun replaceQRCode(vcJson: String, svgTemplate: String): String {
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

    fun replaceBenefits(jsonObject: JSONObject, svgTemplate: String): String {
        return try {
            val credentialSubject = jsonObject.optJSONObject("credentialSubject") ?: return svgTemplate

            val benefitsArray = credentialSubject.optJSONArray("benefits") ?: return svgTemplate

            val benefitsString = (0 until benefitsArray.length())
                .mapNotNull { benefitsArray.optString(it).takeIf { it.isNotEmpty() } }
                .joinToString(",")

            val benefitsPlaceholderList = listOf(BENEFITS_PLACEHOLDER_1, BENEFITS_PLACEHOLDER_2)

            wrapBasedOnCharacterLength(svgTemplate, benefitsString, 55, benefitsPlaceholderList)
        } catch (e: Exception) {
            e.printStackTrace()
            svgTemplate
        }
    }


    fun replaceAddress(jsonObject: JSONObject, svgTemplate: String): String {
        return try {

            val credentialSubject = jsonObject.optJSONObject("credentialSubject") ?: return svgTemplate

            val fields = listOf(ADDRESS_LINE_1, ADDRESS_LINE_2,
                ADDRESS_LINE_3, CITY, PROVINCE, POSTAL_CODE, REGION)
            val values = mutableListOf<String>()

            for (field in fields) {
                val array = credentialSubject.optJSONArray(field)
                if (array != null && array.length() > 0) {
                    val value = array.optJSONObject(0)?.optString("value", "")?.trim()
                    if (value != null) {
                        if (value.isNotEmpty()) {
                            values.add(value)
                        }
                    }
                }
            }

            val fullAddress = values.joinToString(separator = ", ")

            val addressPlaceholderList = listOf(FULL_ADDRESS_PLACEHOLDER_1, FULL_ADDRESS_PLACEHOLDER_2)

            wrapBasedOnCharacterLength(svgTemplate, fullAddress, 55, addressPlaceholderList)
        } catch (e: Exception) {
            e.printStackTrace()
            svgTemplate
        }
    }


    fun wrapBasedOnCharacterLength(svgTemplate: String,
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

    fun replaceLocaleBasedValue(placeholderKey: String, vcJSONObject: JSONObject): String? {
        try {
            val jsonPath = placeholderKey.substringBefore('_')
            val language = placeholderKey.substringAfter('_')

            val value = getValueFromData(jsonPath, vcJSONObject) as? JSONArray
                ?: return null

            for (i in 0 until value.length()) {
                val jsonObject = value.optJSONObject(i) ?: continue
                if (jsonObject.optString("language") == language) {
                    return jsonObject.optString("value", null)
                }
            }
            return null;
        } catch (e: Exception){
            e.printStackTrace()
            return null;

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