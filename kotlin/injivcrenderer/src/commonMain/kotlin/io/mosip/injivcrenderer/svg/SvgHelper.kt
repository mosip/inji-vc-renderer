package io.mosip.injivcrenderer.svg

import io.mosip.injivcrenderer.Constants.DEFAULT_LOCALE
import io.mosip.injivcrenderer.Constants.ID
import io.mosip.injivcrenderer.Constants.QR_CODE_PLACEHOLDER
import io.mosip.injivcrenderer.Constants.QR_IMAGE_PREFIX
import io.mosip.injivcrenderer.Constants.RENDER_PROPERTY
import io.mosip.injivcrenderer.Constants.RENDER_SUITE
import io.mosip.injivcrenderer.Constants.SVG_MUSTACHE
import io.mosip.injivcrenderer.Constants.TEMPLATE
import io.mosip.injivcrenderer.Constants.TEMPLATE_RENDER_METHOD
import io.mosip.injivcrenderer.Constants.TYPE
import io.mosip.injivcrenderer.NetworkHandler
import io.mosip.injivcrenderer.QrCodeGenerator
import io.mosip.injivcrenderer.decodeSvgDataUriToSvgTemplate
import io.mosip.injivcrenderer.svg.SvgPlaceholderHelper.preserveRenderProperty
import org.json.JSONArray
import org.json.JSONObject


object SvgHelper {

    fun extractSvgTemplate(renderMethod: JSONObject, vcJsonString: String): String? {
        if (!isSvgMustacheTemplate(renderMethod)) return null

        return when {
            renderMethod.has(TEMPLATE) -> {
                var svgTemplate: String? = null

                when (val templateValue = renderMethod.get(TEMPLATE)) {
                    is JSONObject -> {
                        if (templateValue.has(ID)) {
                            var rawSvg = NetworkHandler().fetchSvgAsText(templateValue.getString(ID))

                            if (templateValue.has(QR_CODE_PLACEHOLDER)) {
                                rawSvg = injectQrCodePlaceholder(rawSvg, vcJsonString)
                            }

                            if (templateValue.has(RENDER_PROPERTY)) {
                                val renderProps = jsonArrayToList(templateValue.getJSONArray(RENDER_PROPERTY))
                                rawSvg = preserveRenderProperty(rawSvg, renderProps)
                            }

                            svgTemplate = rawSvg
                        }
                    }
                    is String -> {
                        var rawSvg = decodeSvgDataUriToSvgTemplate(templateValue)

                        if (renderMethod.has(QR_CODE_PLACEHOLDER)) {
                            rawSvg = injectQrCodePlaceholder(rawSvg.orEmpty(), vcJsonString)
                        }

                        svgTemplate = rawSvg
                    }
                }

                svgTemplate
            }
            else -> null
        }
    }


    private fun injectQrCodePlaceholder(svgTemplate: String, vcJsonString: String): String {
        val qrBase64 = QrCodeGenerator().generateQRCodeImage(vcJsonString)
        val qrImageTag = "$QR_IMAGE_PREFIX,$qrBase64"
        return svgTemplate.replace(QR_CODE_PLACEHOLDER, qrImageTag)
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