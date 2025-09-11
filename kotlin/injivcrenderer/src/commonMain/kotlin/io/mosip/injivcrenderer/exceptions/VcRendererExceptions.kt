package io.mosip.injivcrenderer.exceptions

import io.mosip.injivcrenderer.constants.Constants.SVG_MUSTACHE
import io.mosip.injivcrenderer.constants.Constants.TEMPLATE_RENDER_METHOD
import io.mosip.injivcrenderer.constants.VcRendererErrorCodes.INVALID_RENDER_METHOD
import io.mosip.injivcrenderer.constants.VcRendererErrorCodes.INVALID_RENDER_METHOD_TYPE
import io.mosip.injivcrenderer.constants.VcRendererErrorCodes.INVALID_RENDER_SUITE
import io.mosip.injivcrenderer.constants.VcRendererErrorCodes.MISSING_TEMPLATE_ID
import io.mosip.injivcrenderer.constants.VcRendererErrorCodes.MULTIBASE_VERIFICATION_FAILED
import io.mosip.injivcrenderer.constants.VcRendererErrorCodes.QR_CODE_GENERATION_FAILURE
import io.mosip.injivcrenderer.constants.VcRendererErrorCodes.SVG_FETCH_ERROR
import io.mosip.injivcrenderer.constants.VcRendererErrorCodes.UNSUPPORTED_CREDENTIAL_FORMAT
import java.util.logging.Level
import java.util.logging.Logger

sealed class VcRendererExceptions(
    val errorCode: String,
    override val message: String,
    val className: String,
    val traceabilityId: String
) : Exception("$errorCode : $message") {


    init {
        Logger.getLogger(className).log(
            Level.SEVERE,
            "ERROR [$errorCode] - $message | Class: $className | TraceabilityId: $traceabilityId"
        )
    }

    class InvalidRenderSuiteException(traceabilityId: String, className: String?) :
        VcRendererExceptions(INVALID_RENDER_SUITE, "Render suite must be '$SVG_MUSTACHE'", className.orEmpty(), traceabilityId)

    class InvalidRenderMethodTypeException(traceabilityId: String, className: String?) :
        VcRendererExceptions(INVALID_RENDER_METHOD_TYPE, "Render method type must be '$TEMPLATE_RENDER_METHOD'", className.orEmpty(), traceabilityId)

    class QRCodeGenerationFailureException(traceabilityId: String, exceptionMessage: String, className: String?, ) :
        VcRendererExceptions(QR_CODE_GENERATION_FAILURE, "QR code generation Failed:${exceptionMessage}", className.orEmpty(), traceabilityId)

    class MissingTemplateIdException(traceabilityId: String, className: String?) :
        VcRendererExceptions(MISSING_TEMPLATE_ID, "Template ID is missing in renderMethod", className.orEmpty(), traceabilityId)

    class SvgFetchException(traceabilityId: String, className: String?, exceptionMessage: String) :
        VcRendererExceptions(
            SVG_FETCH_ERROR, "Failed to fetch SVG: $exceptionMessage", className.orEmpty(), traceabilityId)

    class InvalidRenderMethodException(traceabilityId: String, className: String?) :
        VcRendererExceptions(
            INVALID_RENDER_METHOD, "RenderMethod object is invalid", className.orEmpty(), traceabilityId)

    class UnsupportedCredentialFormat(traceabilityId: String, className: String?) :
        VcRendererExceptions(
            UNSUPPORTED_CREDENTIAL_FORMAT, "Only LDP_VC credential format is supported", className.orEmpty(), traceabilityId)

    class MultibaseVerificationException(traceabilityId: String, className: String?, exceptionMessage: String) :
        VcRendererExceptions(
            MULTIBASE_VERIFICATION_FAILED, "Multibase verification failed: $exceptionMessage", className.orEmpty(), traceabilityId)

}