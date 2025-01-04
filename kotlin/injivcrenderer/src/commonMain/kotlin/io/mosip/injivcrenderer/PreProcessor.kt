package io.mosip.injivcrenderer

import io.mosip.pixelpass.PixelPass
import org.json.JSONArray
import org.json.JSONObject

class PreProcessor {

    fun preProcessSvgTemplate(vcJsonString: String, svgTemplate: String): JSONObject {

        var vcJsonObject = JSONObject(vcJsonString)
        var credentialSubject = vcJsonObject.getJSONObject("credentialSubject")
        credentialSubject = replaceFieldsWithLanguage(credentialSubject)


      // Checks for {{qrCodeImage}} for Qr Code Replacement
        if(svgTemplate.contains(QR_CODE_PLACEHOLDER)){
            credentialSubject = credentialSubject.put(getFieldNameFromPlaceholder(QR_CODE_PLACEHOLDER), replaceQRCode(vcJsonString)) ?: return credentialSubject
        }

      //  Checks for {{benefitsLine1}} or {{benefitsLine2}} for Benefits Replacement
        val benefitsPlaceholderRegexPattern = Regex(BENEFITS_PLACEHOLDER_REGEX_PATTERN)
        if(benefitsPlaceholderRegexPattern.containsMatchIn(svgTemplate)) {
            val benefitsPlaceholders = getPlaceholdersList(benefitsPlaceholderRegexPattern, svgTemplate)
            val language = extractLanguageFromPlaceholder(benefitsPlaceholders.first())
            val commaSeparatedBenefitsElements = generateCommaSeparatedString(credentialSubject,
                listOf(BENEFITS_FIELD_NAME), language)

            credentialSubject =  constructObjectBasedOnCharacterLengthChunks(
                MultiLineProperties(dataToSplit = commaSeparatedBenefitsElements, placeholderList = benefitsPlaceholders, maxCharacterLength = 55),
                credentialSubject,
                language
            )

            listOf(BENEFITS_FIELD_NAME).forEach { fieldName ->
                credentialSubject.remove(fieldName)
            }

        }

        //Checks for {{fullAddressLine1/locale}} or {{fullAddressLine1/locale}} for Address Fields Replacement
        val fullAddressRegexPattern = Regex(FULL_ADDRESS_PLACEHOLDER_REGEX_PATTERN)
        if(fullAddressRegexPattern.containsMatchIn(svgTemplate)) {
            val addressFields = listOf(
                ADDRESS_LINE_1, ADDRESS_LINE_2,
                ADDRESS_LINE_3, CITY, PROVINCE, POSTAL_CODE, REGION
            )
            val fullAddressPlaceholders = getPlaceholdersList(fullAddressRegexPattern, svgTemplate)
            val language = extractLanguageFromPlaceholder(fullAddressPlaceholders.first())
            val commaSeparatedAddressFields = generateCommaSeparatedString(credentialSubject,
                addressFields, language)

            credentialSubject =  constructObjectBasedOnCharacterLengthChunks(
                MultiLineProperties(dataToSplit = commaSeparatedAddressFields, placeholderList = fullAddressPlaceholders, maxCharacterLength = 55),
                credentialSubject, language)

            addressFields.forEach { fieldName ->
                credentialSubject.remove(fieldName)
            }

        }
        vcJsonObject.put(CREDENTIAL_SUBJECT_FIELD, credentialSubject)
        return vcJsonObject
    }



    private fun replaceFieldsWithLanguage(jsonObject: JSONObject): JSONObject {
        val keys = jsonObject.keys().asSequence().toList()

        for (key in keys) {
            val value = jsonObject[key]

            when (value) {
                is JSONArray -> {
                    val languageMap = JSONObject()
                    var hasLanguage = false
                    for (i in 0 until value.length()) {
                        val item = value.get(i)
                        if (item is JSONObject) {
                            if (item.has("language")) {
                                hasLanguage = true
                                val newValue = item.getString("value")
                                val newKey = item.getString("language")
                                languageMap.put(newKey, newValue)
                            }
                        }
                    }

                    if (hasLanguage) {
                        jsonObject.put(key, languageMap)
                    }
                }
                is JSONObject -> {
                    replaceFieldsWithLanguage(value)
                }
            }
        }

        return jsonObject
    }

    fun getFieldNameFromPlaceholder(placeholder: String): String {
        val regex = Regex(GET_PLACEHOLDER_REGEX)
        val matchResult = regex.find(placeholder)
        val enclosedValue = matchResult?.groups?.get(1)?.value
        return enclosedValue?.split("/")?.last().orEmpty()
    }

    fun extractLanguageFromPlaceholder(placeholder: String): String {
        val regex = Regex(GET_LANGUAGE_FORM_PLACEHOLDER_REGEX)
        val matchResult = regex.find(placeholder)
        return matchResult?.groups?.get(1)?.value ?: ""
    }

    fun getPlaceholdersList(placeholderRegexPattern: Regex, svgTemplate: String): List<String> {
        val placeholders = mutableListOf<String>()
        val placeholdersMatches = placeholderRegexPattern.findAll(svgTemplate)
        for (match in placeholdersMatches) {
            placeholders.add(match.value)
        }
        return placeholders
    }

    private fun replaceQRCode(vcJson: String): String {
        try {
            val pixelPass = PixelPass()
            val qrData: String = pixelPass.generateQRData(vcJson)

            if (qrData.length <= 10000) {
                return convertQrDataIntoBase64(qrData)
            }
            return ""
        } catch (e: Exception){
            e.printStackTrace()
            return "";
        }

    }

    private fun generateCommaSeparatedString(jsonObject: JSONObject, fieldsToBeCombined: List<String>, language: String): String {
        return fieldsToBeCombined
            .flatMap { field ->
                when {
                    jsonObject.optJSONArray(field) != null -> {
                        val arrayField = jsonObject.optJSONArray(field)!!
                        (0 until arrayField.length()).flatMap { index ->
                            val item = arrayField.opt(index)
                            when {
                                item is String -> listOf(item.takeIf { it.isNotEmpty() })
                                item is JSONObject -> listOf(item.optString("value").takeIf { it.isNotEmpty() })
                                else -> emptyList() // Skip if neither
                            }
                        }
                    }
                    jsonObject.optJSONObject(field) != null -> {
                        val langValue = jsonObject.optJSONObject(field)?.optString(language).takeIf { !it.isNullOrEmpty() }
                        listOf(langValue)
                    }
                    else -> emptyList()
                }
            }
            .filterNotNull()
            .joinToString(", ")
    }



    private fun constructObjectBasedOnCharacterLengthChunks(
        multiLineProperties: MultiLineProperties,
        jsonObject: JSONObject,
        language: String,

        ): JSONObject{
        try {
            val segments = multiLineProperties.dataToSplit.chunked(multiLineProperties.maxCharacterLength).take(multiLineProperties.placeholderList.size)
            multiLineProperties.placeholderList.forEachIndexed { index, placeholder ->
                if (index < segments.size) {
                    val languageSpecificData = if(language.isNotEmpty()){
                        JSONObject().apply {
                            put(language, segments[index]) }
                    } else {
                        segments[index]
                    }
                    jsonObject.put(getFieldNameFromPlaceholder(placeholder), languageSpecificData)
                }
            }
            return jsonObject
        } catch (e: Exception){
            e.printStackTrace()
            return jsonObject
        }
    }

    companion object{
        const val CREDENTIAL_SUBJECT_FIELD = "credentialSubject"


        const val QR_CODE_PLACEHOLDER="{{credentialSubject/qrCodeImage}}"


        const val BENEFITS_FIELD_NAME = "benefits"
        const val BENEFITS_PLACEHOLDER_REGEX_PATTERN = "\\{\\{credentialSubject/benefitsLine\\d+\\}\\}"


        const val FULL_ADDRESS_PLACEHOLDER_REGEX_PATTERN = "\\{\\{credentialSubject/fullAddressLine\\d+/[a-zA-Z]+\\}\\}"
        const val ADDRESS_LINE_1 = "addressLine1"
        const val ADDRESS_LINE_2 = "addressLine2"
        const val ADDRESS_LINE_3 = "addressLine3"
        const val CITY = "city"
        const val PROVINCE = "province"
        const val REGION = "region"
        const val POSTAL_CODE = "postalCode"


        const val GET_PLACEHOLDER_REGEX = "\\{\\{credentialSubject/([^/]+)(?:/[^}]+)?\\}\\}"
        const val GET_LANGUAGE_FORM_PLACEHOLDER_REGEX = """credentialSubject/[^/]+/(\w+)"""

    }
}

data class MultiLineProperties(
    var dataToSplit: String,
    val placeholderList: List<String>,
    val maxCharacterLength: Int
)