package io.mosip.injivcrenderer.templateEngine.svg

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import io.mosip.injivcrenderer.constants.VcRendererErrorCodes.MISSING_JSON_PATH
import java.util.logging.Level
import java.util.logging.Logger

class JsonPointerResolver(private val traceabilityId: String) {
    private val className = JsonPointerResolver::class.simpleName

    /**
     * Replaces placeholders in an SVG template using a Verifiable Credential JSON for values and Wellknown for labels.
     * @param svgTemplate The SVG template containing placeholders in the format {{/json/pointer}} or {{}}
     * @param jsonNode The root JsonNode of the Verifiable Credential or WellKnown Json
     * @param renderProperties Optional list of allowed JSON pointer paths; others will be replaced with "-"
     * @param isLabelPlaceholder If true, leaves label placeholders ({{}}) unchanged; otherwise replaces with "-"
     */
    fun replacePlaceholders(
        svgTemplate: String,
        jsonNode: JsonNode,
        renderProperties: List<String>? = null,
        isLabelPlaceholder: Boolean = false
    ): String {
        return PLACEHOLDER_REGEX.replace(svgTemplate) { match ->
            val pointerPath = match.groups[1]?.value ?: ""
            if (renderProperties != null && pointerPath !in renderProperties) return@replace "-"

            val valueNode: JsonNode? = try {
                if (pointerPath.isEmpty()) jsonNode
                else jsonNode.at(JsonPointer.compile(pointerPath)).takeIf { !it.isMissingNode }
            } catch (e: Exception) {
                Logger.getLogger(className).log(
                    Level.SEVERE,
                    "ERROR [$MISSING_JSON_PATH] - Missing: $pointerPath | Class: $className | TraceabilityId: $traceabilityId"
                )
                null
            }

            when {
                valueNode == null || valueNode.isNull -> {
                    if (pointerPath.startsWith(FALLBACK_PATH)) {
                        return@replace extractFieldName(pointerPath)
                    }
                    if (isLabelPlaceholder) match.value else "-"

                }
                valueNode.isValueNode -> valueNode.asText()
                else -> valueNode.toString()
            }
        }
    }

    private fun extractFieldName(pointerPath: String): String {
        val raw = pointerPath
            .removePrefix(FALLBACK_PATH)
            .substringBefore("/")

        return raw
            .replace(Regex("\\[\\d+\\]"), "")
            .replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .replace(Regex("([A-Z])([A-Z][a-z])"), "$1 $2")
            .split(Regex("[_\\s]+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
    }


    companion object {
        private val PLACEHOLDER_REGEX = Regex("\\{\\{(/[^}]*)\\}\\}|\\{\\{\\}\\}")
        private const val FALLBACK_PATH = "/credential_definition/credentialSubject/"
    }
}
