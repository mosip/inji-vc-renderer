package io.mosip.injivcrenderer

import io.mosip.injivcrenderer.Constants.RENDER_METHOD
import io.mosip.injivcrenderer.svg.SvgHelper.extractSvgTemplate
import io.mosip.injivcrenderer.svg.SvgPlaceholderHelper.replacePlaceholders
import org.json.JSONArray
import org.json.JSONObject


class InjiVcRenderer {

    /**
     * Renders SVG templates defined in the VC's renderMethod section.
     * Supports fetching templates from URLs and data URIs.
     * Replaces placeholders in the templates with values from the VC JSON.
     *
     * @param vcJsonString The Verifiable Credential as a JSON string.
     * @return A list of rendered SVG strings. Empty list if no valid render methods found or on error.
     */
    fun renderSvg(vcJsonString: String): List<String> {
        return try {
            val jsonObject = JSONObject(vcJsonString)

            if(jsonObject.has(RENDER_METHOD).not()) {
                return emptyList()
            }

            val renderMethodArray = when (val renderMethodValue = jsonObject.get(RENDER_METHOD)) {
                is JSONArray -> renderMethodValue
                is JSONObject -> {
                    if (renderMethodValue.length() == 0) return emptyList()
                    JSONArray().put(renderMethodValue)
                }
                else -> JSONArray()
            }

            val results = mutableListOf<String>()
            for (i in 0 until renderMethodArray.length()) {
                val renderMethod = renderMethodArray.getJSONObject(i)

                val svgTemplate = extractSvgTemplate(renderMethod, vcJsonString)
                if (svgTemplate != null) {
                    val renderedSvg = replacePlaceholders(svgTemplate, jsonObject)
                    results.add(renderedSvg)
                }
            }
            results
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

}