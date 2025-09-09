package io.mosip.injivcrenderer.utils

import com.fasterxml.jackson.databind.JsonNode
import io.mosip.injivcrenderer.networkManager.NetworkManager
import io.mosip.injivcrenderer.constants.Constants.ID
import io.mosip.injivcrenderer.constants.Constants.QR_CODE_PLACEHOLDER
import io.mosip.injivcrenderer.constants.Constants.QR_IMAGE_PREFIX
import io.mosip.injivcrenderer.constants.Constants.RENDER_METHOD
import io.mosip.injivcrenderer.constants.Constants.RENDER_SUITE
import io.mosip.injivcrenderer.constants.Constants.SVG_MUSTACHE
import io.mosip.injivcrenderer.constants.Constants.TEMPLATE
import io.mosip.injivcrenderer.constants.Constants.TEMPLATE_RENDER_METHOD
import io.mosip.injivcrenderer.constants.Constants.TYPE
import io.mosip.injivcrenderer.exceptions.VcRendererExceptions
import io.mosip.injivcrenderer.qrCode.QrCodeGenerator

class SvgHelper(private val traceabilityId: String) {
    private val className = SvgHelper::class.simpleName

    fun extractSvgTemplate(renderMethod: JsonNode, vcJsonString: String): String {
        if (!isSvgMustacheRenderSuite(renderMethod)) {
            throw VcRendererExceptions.InvalidRenderSuiteException(traceabilityId, className)
        }

        if (!isTemplateRenderMethodType(renderMethod)) {
            throw VcRendererExceptions.InvalidRenderMethodTypeException(traceabilityId, className)
        }

        val templateValue = renderMethod.path(TEMPLATE)

        val templateId = templateValue.path(ID).asText(null)
            ?: throw VcRendererExceptions.MissingTemplateIdException(traceabilityId, className)

        var rawSvg = NetworkManager(traceabilityId).fetchSvgAsText(templateId)

        rawSvg = injectQrCodeIfNeeded(rawSvg, vcJsonString)

        return rawSvg
    }

    /** Inject QR code placeholder if present in the SVG */
    private fun injectQrCodeIfNeeded(svg: String, vcJsonString: String): String {
        return try {
            val qrBase64 = QrCodeGenerator(traceabilityId).generateQRCodeImage(vcJsonString)
            val qrImageTag = "$QR_IMAGE_PREFIX,$qrBase64"
            svg.replace(QR_CODE_PLACEHOLDER, qrImageTag)

        } catch (e: Exception) {
            val fallbackBase64 = DEFAULT_FALLBACK_QR_BASE64
            svg.replace(QR_CODE_PLACEHOLDER, "$QR_IMAGE_PREFIX,$fallbackBase64")

        }
    }

    private fun isSvgMustacheRenderSuite(renderMethod: JsonNode): Boolean {
        val renderSuite = renderMethod.path(RENDER_SUITE).asText("")
        return renderSuite == SVG_MUSTACHE
    }

    private fun isTemplateRenderMethodType(renderMethod: JsonNode): Boolean {
        val type = renderMethod.path(TYPE).asText("")
        return type == TEMPLATE_RENDER_METHOD
    }

    fun parseRenderMethod(jsonObject: JsonNode, traceabilityId: String): List<JsonNode> {
        val renderMethodValue = jsonObject.path(RENDER_METHOD)

        return when {
            renderMethodValue.isArray -> {
                val elements = renderMethodValue.toList()
                if (elements.isEmpty() || elements.any { !it.isObject || it.size() == 0 }) {
                    throw VcRendererExceptions.InvalidRenderMethodException(
                        traceabilityId,
                        className
                    )
                }
                elements
            }

            renderMethodValue.isObject -> {
                if (renderMethodValue.size() == 0) {
                    throw VcRendererExceptions.InvalidRenderMethodException(
                        traceabilityId,
                        className
                    )
                }
                listOf(renderMethodValue)
            }

            else -> throw VcRendererExceptions.InvalidRenderMethodException(
                traceabilityId,
                className
            )
        }
    }


    companion object {
        const val DEFAULT_FALLBACK_QR_BASE64 = "default_fallback_qr_code_base64_string"
    }
}
