package io.mosip.injivcrenderer

import com.fasterxml.jackson.databind.JsonNode
import io.mosip.injivcrenderer.Constants.ID
import io.mosip.injivcrenderer.Constants.QR_CODE_PLACEHOLDER
import io.mosip.injivcrenderer.Constants.QR_IMAGE_PREFIX
import io.mosip.injivcrenderer.Constants.RENDER_METHOD
import io.mosip.injivcrenderer.Constants.RENDER_SUITE
import io.mosip.injivcrenderer.Constants.SVG_MUSTACHE
import io.mosip.injivcrenderer.Constants.TEMPLATE
import io.mosip.injivcrenderer.Constants.TEMPLATE_RENDER_METHOD
import io.mosip.injivcrenderer.Constants.TYPE

object SvgHelper {

    fun extractSvgTemplate(renderMethod: JsonNode, vcJsonString: String): String {
        if (!isSvgMustacheTemplate(renderMethod)) return ""

        val templateValue = renderMethod.path(TEMPLATE)
        val templateId = templateValue.path(ID).asText(null) ?: return ""

        var rawSvg = NetworkHandler().fetchSvgAsText(templateId)

        rawSvg = injectQrCodeIfNeeded(rawSvg, vcJsonString)

        return rawSvg
    }

    /** Inject QR code placeholder if present in the SVG */
    private fun injectQrCodeIfNeeded(svg: String, vcJsonString: String): String {
        if (!svg.contains(QR_CODE_PLACEHOLDER)) return svg
        val qrBase64 = QrCodeGenerator().generateQRCodeImage(vcJsonString)
        val qrImageTag = "$QR_IMAGE_PREFIX,$qrBase64"
        return svg.replace(QR_CODE_PLACEHOLDER, qrImageTag)
    }

    private fun isSvgMustacheTemplate(renderMethod: JsonNode): Boolean {
        val type = renderMethod.path(TYPE).asText("")
        val renderSuite = renderMethod.path(RENDER_SUITE).asText("")
        return type == TEMPLATE_RENDER_METHOD && renderSuite == SVG_MUSTACHE
    }

    fun parseRenderMethod(jsonObject: JsonNode): List<JsonNode> {
        val renderMethodValue = jsonObject.path(RENDER_METHOD)
        return when {
            renderMethodValue.isArray -> renderMethodValue.toList()
            renderMethodValue.isObject -> listOf(renderMethodValue)
            else -> emptyList()
        }
    }
}
