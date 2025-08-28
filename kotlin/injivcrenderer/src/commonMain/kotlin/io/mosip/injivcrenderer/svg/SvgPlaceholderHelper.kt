package io.mosip.injivcrenderer.svg

import io.mosip.injivcrenderer.Constants.ADDRESS_LINE_1
import io.mosip.injivcrenderer.Constants.ADDRESS_LINE_2
import io.mosip.injivcrenderer.Constants.ADDRESS_LINE_3
import io.mosip.injivcrenderer.Constants.CITY
import io.mosip.injivcrenderer.Constants.CONCATENATED_ADDRESS
import io.mosip.injivcrenderer.Constants.CREDENTIAL_SUBJECT
import io.mosip.injivcrenderer.Constants.DEFAULT_LOCALE
import io.mosip.injivcrenderer.Constants.POSTAL_CODE
import io.mosip.injivcrenderer.Constants.PROVINCE
import io.mosip.injivcrenderer.Constants.REGION
import io.mosip.injivcrenderer.svg.SvgHelper.extractSvgLocale
import io.mosip.injivcrenderer.svg.SvgHelper.extractSvgWidth
import io.mosip.injivcrenderer.svg.SvgMultilineFormatter.chunkAddressFields
import io.mosip.injivcrenderer.svg.SvgMultilineFormatter.chunkArrayFields
import org.json.JSONArray
import org.json.JSONObject

object SvgPlaceholderHelper {

    private const val PLACEHOLDER_REGEX_PATTERN = "\\{\\{(/[^}]+)\\}\\}"

    fun replacePlaceholders(svgTemplate: String, jsonObject: JSONObject): String {
        val regex = Regex(PLACEHOLDER_REGEX_PATTERN)
        val svgWidth = extractSvgWidth(svgTemplate) ?: 100
        val locale = extractSvgLocale(svgTemplate)


        return regex.replace(svgTemplate) { match ->
            val path = match.groups[1]?.value ?: ""
            val value = getValueFromJsonPath(jsonObject, path, svgWidth, locale = locale)
            value?.toString() ?: "-"
        }
    }

    fun preserveRenderProperty(svgTemplate: String, renderProperties: List<String>): String {

        val regex = Regex("""\{\{[^}]+}}""")

        return regex.replace(svgTemplate) { match ->
            val placeholder = match.value
            val path = placeholder.removePrefix("{{").removeSuffix("}}").trim()

            // keep if it's in renderProperties, else replace with "-"
            if (renderProperties.contains(path)) {
                placeholder
            } else {
                "-"
            }
        }
    }

    private fun getConcatenatedAddress(credentialSubject: JSONObject, lang: String = "eng"): String {
        val addressFields = listOf(
            ADDRESS_LINE_1, ADDRESS_LINE_2,
            ADDRESS_LINE_3, CITY, PROVINCE, REGION, POSTAL_CODE
        )
        val parts = mutableListOf<String>()

        for (field in addressFields) {
            if (!credentialSubject.has(field)) continue

            val value = when (val fieldValue = credentialSubject.get(field)) {
                is JSONArray -> { // language-based array
                    extractFromLangArray(fieldValue, lang)
                }
                is String -> fieldValue // plain string
                else -> null
            }

            if (!value.isNullOrBlank()) {
                parts.add(value)
            }
        }
        return parts.joinToString(", ")
    }

    private fun extractFromLangArray(array: JSONArray, lang: String): String? {
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            if (obj.optString("language") == lang) {
                return obj.optString("value", null)
            }
        }
        // fallback: return first "value" if no match
        return array.optJSONObject(0)?.optString("value")
    }

    fun getValueFromJsonPath(
        jsonObject: JSONObject,
        path: String,
        svgWidth: Int,
        isDefaultHandled: Boolean = false,
        locale: String = DEFAULT_LOCALE
    ): Any? {
        val keys = path.trimStart('/').split("/")

        if (keys.last() == CONCATENATED_ADDRESS) {
            val subject = jsonObject.optJSONObject(CREDENTIAL_SUBJECT) ?: return null
            val address = getConcatenatedAddress(subject, locale)
            return chunkAddressFields(address, svgWidth)
        }

        var current: Any? = jsonObject

        for (k in keys) {
            current = when (current) {
                is JSONObject -> current.opt(k)
                is JSONArray -> {
                    val index = k.toIntOrNull()
                    if (index != null && index < current.length()) current.opt(index) else null
                }
                else -> null
            }

            if (current == null && !isDefaultHandled) {
                // fallback to English
                val parentPath = keys.dropLast(1).joinToString("/")
                return getValueFromJsonPath(jsonObject, "$parentPath/$DEFAULT_LOCALE", svgWidth, true)
            }
        }

        return when (current) {
            is JSONObject -> {
                val lastKey = keys.last()
                current.opt(lastKey) ?: current.opt(DEFAULT_LOCALE)
            }
            is JSONArray -> chunkArrayFields(current, svgWidth)
            else -> current
        }
    }
}