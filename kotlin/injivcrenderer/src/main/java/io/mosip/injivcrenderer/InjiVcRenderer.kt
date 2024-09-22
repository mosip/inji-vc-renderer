package io.mosip.injivcrenderer

import io.mosip.injivcrenderer.Utils.fetchSvgAsText
import org.json.JSONArray
import org.json.JSONObject


class InjiVcRenderer {

    private val templatePreProcessor = TemplatePreProcessor()


    fun renderSvg(vcJsonString: String): String {
        return try {
            val jsonObject = JSONObject(vcJsonString)
            val renderMethodArray = jsonObject.getJSONArray("renderMethod")
            val svgUrl = renderMethodArray.getJSONObject(0).getString("id")

            var svgTemplate = fetchSvgAsText(svgUrl)
            val processedJson = templatePreProcessor.preProcessSvgTemplate(vcJsonString, svgTemplate)

            replacePlaceholders(svgTemplate, processedJson)

        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun replacePlaceholders(svgTemplate: String, processedJson: JSONObject): String {
        val regex = Regex(PLACEHOLDER_REGEX_PATTERN)
        return regex.replace(svgTemplate) { match ->
            val key = match.groups[1]?.value?.trim() ?: ""
            val value = getValueFromData(key, processedJson)
            value?.toString() ?: ""
        }
    }

    fun getValueFromData(key: String, jsonObject: JSONObject, isDefaultLanguageHandle: Boolean = false): Any? {
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

        //Setting Default Language to English
        return when {
            currentValue is JSONObject -> currentValue.opt(DEFAULT_ENG) ?: null
            currentValue == null && keys.isNotEmpty() && !isDefaultLanguageHandle -> {
                    val updatedKey = keys.dropLast(1).joinToString("/") + "/${DEFAULT_ENG}"
                    getValueFromData(updatedKey, jsonObject, true)
            }
            else -> currentValue
        }
    }

    companion object{
        const val PLACEHOLDER_REGEX_PATTERN = "\\{\\{([^}]+)\\}\\}"
        const val DEFAULT_ENG = "eng"

    }
}