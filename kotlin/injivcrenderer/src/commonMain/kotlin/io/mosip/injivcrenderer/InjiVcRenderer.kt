package io.mosip.injivcrenderer

import io.mosip.injivcrenderer.Constants.RENDER_PROPERTY
import io.mosip.injivcrenderer.Constants.TEMPLATE
import io.mosip.injivcrenderer.JsonPointerResolver.replacePlaceholders
import io.mosip.injivcrenderer.SvgHelper.extractSvgTemplate
import io.mosip.injivcrenderer.SvgHelper.parseRenderMethod
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

class InjiVcRenderer {

    private val mapper = ObjectMapper()

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
            val vcJsonNode: JsonNode = mapper.readTree(vcJsonString)
            val renderMethodArray = parseRenderMethod(vcJsonNode)

            val results = mutableListOf<String>()
            for (element in renderMethodArray) {

                val svgTemplate = extractSvgTemplate(element, vcJsonString)
                if (svgTemplate.isNotEmpty()) {
                    val renderProperties =
                        element.path(TEMPLATE).path(RENDER_PROPERTY).takeIf { it.isArray }?.map { it.asText() }
                    val renderedSvg = replacePlaceholders(svgTemplate, vcJsonNode, renderProperties)
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