package io.mosip.injivcrenderer

import io.mosip.injivcrenderer.Constants.ID
import io.mosip.injivcrenderer.Constants.QR_CODE_PLACEHOLDER
import io.mosip.injivcrenderer.Constants.QR_IMAGE_PREFIX
import io.mosip.injivcrenderer.Constants.RENDER_METHOD
import io.mosip.injivcrenderer.Constants.RENDER_SUITE
import io.mosip.injivcrenderer.Constants.SVG_MUSTACHE
import io.mosip.injivcrenderer.Constants.TEMPLATE
import io.mosip.injivcrenderer.Constants.TEMPLATE_RENDER_METHOD
import io.mosip.injivcrenderer.Constants.TYPE
import org.json.JSONArray
import org.json.JSONObject


object SvgHelper {

    fun extractSvgTemplate(renderMethod: JSONObject, vcJsonString: String): String {
        if (!isSvgMustacheTemplate(renderMethod)) return ""

        val templateValue = renderMethod.optJSONObject(TEMPLATE) ?: return ""
        val templateId = templateValue.optString(ID, null) ?: return ""

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
    fun isSvgMustacheTemplate(renderMethod: JSONObject): Boolean {
        val type = renderMethod.optString(TYPE, "")
        val renderSuite = renderMethod.optString(RENDER_SUITE, "")
        return type == TEMPLATE_RENDER_METHOD && renderSuite == SVG_MUSTACHE
    }

    fun parseRenderMethod(jsonObject: JSONObject): JSONArray {
        if (!jsonObject.has(RENDER_METHOD)) return JSONArray()

        return when (val renderMethodValue = jsonObject.get(RENDER_METHOD)) {
            is JSONArray -> renderMethodValue
            is JSONObject -> JSONArray().put(renderMethodValue)
            else -> JSONArray()
        }
    }


}
