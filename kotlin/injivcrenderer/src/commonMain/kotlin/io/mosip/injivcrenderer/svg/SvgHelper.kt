package io.mosip.injivcrenderer.svg

import io.mosip.injivcrenderer.Constants.DEFAULT_LOCALE
import io.mosip.injivcrenderer.Constants.ID
import io.mosip.injivcrenderer.Constants.RENDER_PROPERTY
import io.mosip.injivcrenderer.Constants.RENDER_SUITE
import io.mosip.injivcrenderer.Constants.SVG_MUSTACHE
import io.mosip.injivcrenderer.Constants.TEMPLATE
import io.mosip.injivcrenderer.Constants.TEMPLATE_RENDER_METHOD
import io.mosip.injivcrenderer.Constants.TYPE
import io.mosip.injivcrenderer.NetworkHandler
import io.mosip.injivcrenderer.decodeSvgDataUriToSvgTemplate
import io.mosip.injivcrenderer.svg.SvgPlaceholderHelper.preserveRenderProperty
import org.json.JSONArray
import org.json.JSONObject


object SvgHelper {

     fun extractSvgTemplate(renderMethod: JSONObject): String? {
        if (!isSvgMustacheTemplate(renderMethod)) return null

        return when {
            renderMethod.has(TEMPLATE) -> {
                when (val templateValue = renderMethod.get(TEMPLATE)) {
                    is JSONObject -> {
                        if (templateValue.has(ID)) {
                            val svgUrl = templateValue.getString(ID)
                            var svgTemplate = NetworkHandler().fetchSvgAsText(svgUrl)
                            if (templateValue.has(RENDER_PROPERTY)) {
                                val allowedProps = templateValue.getJSONArray(RENDER_PROPERTY)
                                svgTemplate = preserveRenderProperty(svgTemplate, jsonArrayToList(allowedProps))
                            }
                            svgTemplate
                        } else null
                    }
                    is String -> decodeSvgDataUriToSvgTemplate(templateValue)
                    else -> null
                }
            }
            else -> null
        }
    }

     fun extractSvgLocale(svgTemplate: String): String {
        val regex = Regex("""<svg[^>]*\blang\s*=\s*["']([a-zA-Z-]+)["']""")
        val match = regex.find(svgTemplate)
        return match?.groups?.get(1)?.value ?: DEFAULT_LOCALE
    }

    fun extractSvgWidth(svgTemplate: String): Int? {
        val regex = Regex("""<svg[^>]*\bwidth\s*=\s*["'](\d+)(px)?["']""")
        val match = regex.find(svgTemplate)
        return match?.groups?.get(1)?.value?.toIntOrNull()
    }

    fun isSvgMustacheTemplate(renderMethod: JSONObject): Boolean {
        val type = renderMethod.optString(TYPE, "")
        val renderSuite = renderMethod.optString(RENDER_SUITE, "")
        return type == TEMPLATE_RENDER_METHOD && renderSuite == SVG_MUSTACHE
    }


    fun jsonArrayToList(jsonArray: JSONArray): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getString(i))
        }
        return list
    }
}