package io.mosip.injivcrenderer

import kotlinx.serialization.json.*
import org.json.JSONArray
import org.json.JSONObject

object JsonPointerResolver {

    private val PLACEHOLDER_REGEX = Regex("\\{\\{(/[^}]*)\\}\\}|\\{\\{\\}\\}")

    /**
     * Replaces placeholders in an SVG template using a Verifiable Credential JSON.
     * Supports optional whitelist of allowed placeholders.
     */
    fun replacePlaceholders(
        svgTemplate: String,
        vcJSONObject: JSONObject,
        renderProperties: List<String>? = null
    ): String {
        val vcJsonElement = orgJsonToJsonElement(vcJSONObject)

        return PLACEHOLDER_REGEX.replace(svgTemplate) { match ->
            val pointerPath = match.groups[1]?.value ?: ""
            if (renderProperties != null && pointerPath !in renderProperties) return@replace "-"

            val value = try {
                if (pointerPath.isEmpty()) vcJsonElement
                else resolvePointer(vcJsonElement, pointerPath)
            } catch (e: Exception) {
                null
            }

            when (value) {
                null, JsonNull -> "-"
                is JsonPrimitive -> value.content
                is JsonObject, is JsonArray -> value.toString()
                else -> value.toString()
            }
        }
    }


    private fun orgJsonToJsonElement(value: Any?): JsonElement = when (value) {
        is JSONObject -> buildJsonObject {
            value.keys().forEach { key ->
                put(key, orgJsonToJsonElement(value[key]))
            }
        }
        is JSONArray -> buildJsonArray {
            for (i in 0 until value.length()) {
                add(orgJsonToJsonElement(value[i]))
            }
        }
        is Boolean -> JsonPrimitive(value)
        is Number -> JsonPrimitive(value)
        is String -> JsonPrimitive(value)
        JSONObject.NULL, null -> JsonNull
        else -> JsonPrimitive(value.toString())
    }

    /**
     * Resolves a JSON Pointer (RFC 6901) against a JsonElement
     */
    private fun resolvePointer(root: JsonElement, pointer: String): JsonElement? {
        if (pointer.isEmpty()) return root // root itself

        val parts = pointer.trimStart('/').split("/").map {
            it.replace("~1", "/").replace("~0", "~")
        }

        var current: JsonElement = root
        for (part in parts) {
            current = when (current) {
                is JsonObject -> current[part] ?: return null
                is JsonArray -> {
                    val idx = part.toIntOrNull() ?: return null
                    current.getOrNull(idx) ?: return null
                }
                else -> return null
            }
        }

        return current
    }
}
