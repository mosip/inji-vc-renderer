package io.mosip.injivcrenderer

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode

object JsonPointerResolver {

    private val PLACEHOLDER_REGEX = Regex("\\{\\{(/[^}]*)\\}\\}|\\{\\{\\}\\}")

    /**
     * Replaces placeholders in an SVG template using a Verifiable Credential JSON.
     * Supports optional whitelist of allowed placeholders.
     */
    fun replacePlaceholders(
        svgTemplate: String,
        vcJsonNode: JsonNode,
        renderProperties: List<String>? = null
    ): String {
        return PLACEHOLDER_REGEX.replace(svgTemplate) { match ->
            val pointerPath = match.groups[1]?.value ?: ""
            if (renderProperties != null && pointerPath !in renderProperties) return@replace "-"

            val valueNode: JsonNode? = try {
                if (pointerPath.isEmpty()) vcJsonNode
                else vcJsonNode.at(JsonPointer.compile(pointerPath)).takeIf { !it.isMissingNode }
            } catch (e: Exception) {
                null
            }

            when {
                valueNode == null || valueNode.isNull -> "-"
                valueNode.isValueNode -> valueNode.asText()
                else -> valueNode.toString()
            }
        }
    }
}
