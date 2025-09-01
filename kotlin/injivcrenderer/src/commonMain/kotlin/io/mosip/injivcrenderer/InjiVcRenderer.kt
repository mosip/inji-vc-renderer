package io.mosip.injivcrenderer

import io.mosip.injivcrenderer.Constants.RENDER_PROPERTY
import io.mosip.injivcrenderer.Constants.TEMPLATE
import io.mosip.injivcrenderer.JsonPointerResolver.replacePlaceholders
import io.mosip.injivcrenderer.SvgHelper.extractSvgTemplate
import io.mosip.injivcrenderer.SvgHelper.parseRenderMethod
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
            val vcJsonObject = JSONObject(vcJsonString)
            val renderMethodArray = parseRenderMethod(vcJsonObject)

            val results = mutableListOf<String>()
            for (i in 0 until renderMethodArray.length()) {
                val renderMethod = renderMethodArray.getJSONObject(i)

                val svgTemplate = extractSvgTemplate(renderMethod, vcJsonString)
                if (svgTemplate.isNotEmpty()) {
                    val renderProperties = renderMethod.optJSONObject(TEMPLATE)?.optJSONArray(
                        RENDER_PROPERTY)?.let {
                        List(it.length()) { idx -> it.getString(idx) }
                    }
                    val renderedSvg = replacePlaceholders(svgTemplate, vcJsonObject, renderProperties)
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