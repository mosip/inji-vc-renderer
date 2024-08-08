package io.mosip.injivcrenderer

interface InjiVcRendererInterface {
    fun replaceSVGTemplatePlaceholders(svgTemplate: String, vcJsonString: String): String
}