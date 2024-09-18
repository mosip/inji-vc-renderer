package io.mosip.injivcrenderer

import io.mosip.injivcrenderer.Utils.fetchSvgAsText
import io.mosip.injivcrenderer.Utils.getValueBasedOnLanguage
import org.json.JSONArray
import org.json.JSONObject


class InjiVcRenderer {

    private val templatePreProcessor = TemplatePreProcessor()


    fun renderSvg(vcJsonString: String): String {
        try {
            val jsonObject = JSONObject(vcJsonString)
            val renderMethodArray = jsonObject.getJSONArray("renderMethod")
            val firstRenderMethod = renderMethodArray.getJSONObject(0)
            val svgUrl = firstRenderMethod.getString("id")

            var svgTemplate = fetchSvgAsText(svgUrl)
            svgTemplate = templatePreProcessor.preProcessSvgTemplate(vcJsonString, svgTemplate)

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

    fun replaceLocaleBasedValue(placeholderKey: String, vcJSONObject: JSONObject): String? {
        try {
            val jsonPath = placeholderKey.substringBefore('_')
            val language = placeholderKey.substringAfter('_')
            val arrayOfObjects = getValueFromData(jsonPath, vcJSONObject) as? JSONArray
                ?: return null
            return getValueBasedOnLanguage(arrayOfObjects, language)

        } catch (e: Exception){
            e.printStackTrace()
            return null;
        }
    }



    companion object{
        const val PLACEHOLDER_REGEX_PATTERN = "\\{\\{([^}]+)\\}\\}"

    }
}