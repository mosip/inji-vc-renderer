package io.mosip.injivcrenderer
import io.mosip.injivcrenderer.DateUtils.formatDate
import io.mosip.injivcrenderer.DateUtils.isValidDateTime
import io.mosip.injivcrenderer.NetworkHelper.fetchSvgAsText
import okio.IOException
import org.json.JSONObject

class InjiVcRenderer {

    // Method to get value from nested JSON data
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

    // Method to replace placeholders in the SVG template
    fun renderSvg(vcJsonData: String): String {
        try {
            val jsonObject = JSONObject(vcJsonData)
            val renderMethodArray = jsonObject.getJSONArray("renderMethod")
            val firstRenderMethod = renderMethodArray.getJSONObject(0)
            val svgUrl = firstRenderMethod.getString("id")

            val svgTemplate = fetchSvgAsText(svgUrl)

            val regex = Regex("\\{\\{(.*?)\\}\\}")

            // Replace placeholders in the SVG template
            return regex.replace(svgTemplate) { match ->
                val key = match.groups[1]?.value?.trim() ?: ""
                val value = getValueFromData(key, jsonObject)
                when {
                    value is String && isValidDateTime(value) -> formatDate(value)
                    value != null -> value.toString()
                    else -> ""
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }
    }
}
