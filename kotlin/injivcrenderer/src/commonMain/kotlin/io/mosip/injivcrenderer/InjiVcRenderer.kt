package io.mosip.injivcrenderer

import io.mosip.injivcrenderer.constants.Constants.RENDER_PROPERTY
import io.mosip.injivcrenderer.constants.Constants.TEMPLATE
import io.mosip.injivcrenderer.templateEngine.svg.JsonPointerResolver
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.mosip.injivcrenderer.exceptions.VcRendererExceptions
import io.mosip.injivcrenderer.utils.SvgHelper

class InjiVcRenderer(private val traceabilityId: String) {

    private val mapper = ObjectMapper()

    /**
     * Renders SVG templates defined in the VC's renderMethod section.
     * Supports fetching templates from URLs and data URIs.
     * Replaces placeholders in the templates with values from the VC JSON.
     *
     * @param vcJsonString The Verifiable Credential as a JSON string.
     * @return A list of rendered SVG strings. Empty list if no valid render methods found or on error.
     */
    fun renderVC(vcJsonString: String): List<String> {
        return try {
            val vcJsonNode: JsonNode = mapper.readTree(vcJsonString)
            val renderMethodArray = SvgHelper(traceabilityId).parseRenderMethod(vcJsonNode, traceabilityId)

            val results = mutableListOf<String>()
            for (element in renderMethodArray) {

                val svgTemplate = SvgHelper(traceabilityId).extractSvgTemplate(element, vcJsonString)
                val renderProperties =
                    element.path(TEMPLATE).path(RENDER_PROPERTY).takeIf { it.isArray }?.map { it.asText() }
                val renderedSvg = JsonPointerResolver(traceabilityId).replacePlaceholders(svgTemplate, vcJsonNode, renderProperties)
                results.add(renderedSvg)

            }
            results
        } catch (vcRendererException : VcRendererExceptions) {
            throw vcRendererException
        }
    }
}