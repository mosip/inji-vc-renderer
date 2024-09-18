package io.mosip.injivcrenderer

import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InjiVcRendererTest {
    private val svgUrl = "http://example.com/svg"

    private val injivcRenderer = InjiVcRenderer()

    @Test
    fun `renderSvg handles missing renderMethod`() {
        val vcJsonString = """{}"""

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals("", result)
    }

    @Test
    fun `renderSvg handles invalid JSON input`() {
        val vcJsonString = """{ "renderMethod": [ "invalid" ] }"""

        val result = injivcRenderer.renderSvg(vcJsonString)

        assertEquals("", result)
    }


}
