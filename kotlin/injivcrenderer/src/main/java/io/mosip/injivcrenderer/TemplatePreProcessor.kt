package io.mosip.injivcrenderer

import android.graphics.Bitmap
import android.util.Base64
import io.mosip.injivcrenderer.Utils.getValueBasedOnLanguage
import io.mosip.pixelpass.PixelPass
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class TemplatePreProcessor {

    fun preProcessSvgTemplate(vcJsonString: String, svgTemplate: String): String {

        var preProcessedSvgTemplate = svgTemplate

        //Checks for {{qrCodeImage}} for Qr Code Replacement
        if(svgTemplate.contains(QR_CODE_PLACEHOLDER)){
            preProcessedSvgTemplate =  replaceQRCode(vcJsonString, svgTemplate)
        }

        //Checks for {{benefits1}} or {{benefits2}} for Benefits Replacement
        val benefitsPlaceholderRegexPattern = Regex(BENEFITS_PLACEHOLDER_REGEX_PATTERN)
        if(benefitsPlaceholderRegexPattern.containsMatchIn(svgTemplate)) {
            val benefitsPlaceholders = getPlaceholdersList(benefitsPlaceholderRegexPattern, preProcessedSvgTemplate)
            preProcessedSvgTemplate =  transformArrayFieldsIntoMultiline(
                JSONObject(vcJsonString),
                preProcessedSvgTemplate,
                MultiLineProperties(benefitsPlaceholders, 55, BENEFITS_FIELD_NAME)
            )
        }

        //Checks for {{fullAddress1_locale}} or {{fullAddress2_locale}} for Address Fields Replacement
        val fullAddressRegexPattern = Regex(FULL_ADDRESS_PLACEHOLDER_REGEX_PATTERN)
        if(fullAddressRegexPattern.containsMatchIn(svgTemplate)) {
            val fullAddressPlaceholders = getPlaceholdersList(fullAddressRegexPattern, preProcessedSvgTemplate);
            preProcessedSvgTemplate =  replaceAddress(
                    JSONObject(vcJsonString),
                    preProcessedSvgTemplate,
                    MultiLineProperties(fullAddressPlaceholders, 55))
        }
        return preProcessedSvgTemplate
    }

    fun getPlaceholdersList(placeholderRegexPattern: Regex, svgTemplate: String): List<String> {
        val placeholders = mutableListOf<String>()
        val placeholdersMatches = placeholderRegexPattern.findAll(svgTemplate)
        for (match in placeholdersMatches) {
            placeholders.add(match.value)
        }
        return placeholders
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


    fun transformArrayFieldsIntoMultiline(
        jsonObject: JSONObject,
        svgTemplate: String,
        multiLineProperties: MultiLineProperties
    ): String {
        return try {
            val credentialSubject = jsonObject.optJSONObject("credentialSubject") ?: return svgTemplate
            val fieldArray = credentialSubject.optJSONArray(multiLineProperties.fieldName) ?: return svgTemplate

            val commaSeparatedFieldElements = (0 until fieldArray.length())
                .mapNotNull { fieldArray.optString(it).takeIf { it.isNotEmpty() } }
                .joinToString(",")

            wrapBasedOnCharacterLength(svgTemplate, commaSeparatedFieldElements, multiLineProperties.maxCharacterLength, multiLineProperties.placeholders)
        } catch (e: Exception) {
            e.printStackTrace()
            svgTemplate
        }
    }


    fun replaceAddress(
        jsonObject: JSONObject,
        svgTemplate: String,
        multiLineProperties: MultiLineProperties
    ): String {
        return try {
            val credentialSubject = jsonObject.optJSONObject("credentialSubject") ?: return svgTemplate
            val fields = listOf(
                ADDRESS_LINE_1, ADDRESS_LINE_2,
                ADDRESS_LINE_3, CITY, PROVINCE, POSTAL_CODE, REGION
            )
            val values = mutableListOf<String>()
            val fullAddressRegexToExtractLanguage = "\\{\\{fullAddress1_(\\w+)\\}\\}".toRegex()
            val language =  fullAddressRegexToExtractLanguage.find(svgTemplate)?.groupValues?.get(1)

            for (field in fields) {
                val array = credentialSubject.optJSONArray(field)
                if (array != null && array.length() > 0) {
                    val value = getValueBasedOnLanguage(array, language.orEmpty())
                    if(value.isNotEmpty()){
                        values.add(value)
                    }
                }
            }

            val fullAddress = values.joinToString(separator = ", ")
            wrapBasedOnCharacterLength(svgTemplate, fullAddress, multiLineProperties.maxCharacterLength, multiLineProperties.placeholders)
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
    companion object{
        const val BASE64_IMAGE_TYPE= "data:image/png;base64,"
        const val QR_CODE_PLACEHOLDER="{{qrCodeImage}}"


        const val BENEFITS_FIELD_NAME = "benefits"
        const val BENEFITS_PLACEHOLDER_REGEX_PATTERN = "\\{\\{benefits\\d+\\}\\}"


        const val FULL_ADDRESS_PLACEHOLDER_REGEX_PATTERN = "\\{\\{fullAddress\\d*_\\w+\\}\\}"
        const val ADDRESS_LINE_1 = "addressLine1"
        const val ADDRESS_LINE_2 = "addressLine2"
        const val ADDRESS_LINE_3 = "addressLine3"
        const val CITY = "city"
        const val PROVINCE = "province"
        const val REGION = "region"
        const val POSTAL_CODE = "postalCode"

    }
}

data class MultiLineProperties(
    val placeholders: List<String>,
    val maxCharacterLength: Int,
    val fieldName: String? = "",
)