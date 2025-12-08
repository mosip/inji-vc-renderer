package io.mosip.injivcrenderer

import com.fasterxml.jackson.databind.ObjectMapper
import io.mosip.injivcrenderer.constants.CredentialFormat
import io.mosip.injivcrenderer.exceptions.VcRendererExceptions
import io.mosip.injivcrenderer.templateEngine.svg.JsonPointerResolver
import io.mosip.injivcrenderer.templateEngine.svg.svgListToPdfBase64
import io.mosip.injivcrenderer.utils.TemplateHelper

class InjiVcRenderer(private val traceabilityId: String) {

    private val mapper = ObjectMapper()
    private val templateHelper = TemplateHelper(traceabilityId)
    private val jsonPointerResolver = JsonPointerResolver(traceabilityId)

    /**
     * Renders SVG templates defined in the VC's renderMethod section.
     * Supports fetching templates from URLs and data URIs.
     * Replaces placeholders in the templates with values from the VC JSON.
     *
     * @param credentialFormat The format of the credential. Currently only LDP_VC is supported.
     * @param wellKnownJson Optional well-known JSON.
     * @param vcJsonString The Verifiable Credential as a JSON string.
     * @param qrCodeData Optional QR code data to embed in the SVG.
     * @return A list of rendered SVG strings.
     */
    @JvmOverloads
    fun generateCredentialDisplayContent(
        credentialFormat: CredentialFormat,
        wellKnownJson: String? = null,
        vcJsonString: String,
        qrCodeData: String? = null
    ): List<Any> {

        if (credentialFormat != CredentialFormat.LDP_VC) {
            throw VcRendererExceptions.UnsupportedCredentialFormat(
                traceabilityId = traceabilityId,
                className = this::class.simpleName
            )
        }

        val vcJsonNode = mapper.readTree(vcJsonString)
        val renderMethodArray = templateHelper.parseRenderMethod(vcJsonNode)

        return renderMethodArray.flatMap { renderMethodElement ->
            templateHelper.extractSVG(renderMethodElement).map { rawSvg ->
                jsonPointerResolver.replaceSvgPlaceholders(rawSvg, vcJsonNode, renderMethodElement, vcJsonString, qrCodeData)
            }
        }
    }

    /** Converts a list of SVG strings to a PDF Base64 string */
    fun convertSvgToPdf(svgList: List<String>) = svgListToPdfBase64(svgList)
}
