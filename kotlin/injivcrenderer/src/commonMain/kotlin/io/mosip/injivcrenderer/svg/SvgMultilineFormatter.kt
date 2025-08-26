package io.mosip.injivcrenderer.svg

import org.json.JSONArray

object SvgMultilineFormatter {


    fun chunkArrayFields(array: JSONArray, svgWidth: Int, avgCharWidth: Int = 8): String {
        val combined = (0 until array.length())
            .mapNotNull { array.opt(it)?.toString() }
            .joinToString(", ")

        return wrapText(combined, svgWidth, avgCharWidth)
    }

    fun chunkAddressFields(address: String, svgWidth: Int, avgCharWidth: Int = 8): String {
        return wrapText(address, svgWidth, avgCharWidth)
    }

    private fun wrapText(text: String, svgWidth: Int, avgCharWidth: Int): String {
        val charsPerLine = (svgWidth / avgCharWidth).coerceAtLeast(1)
        val words = text.split(" ")

        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            if (currentLine.length + word.length + 1 > charsPerLine) {
                lines.add(currentLine.toString().trim())
                currentLine = StringBuilder(word)
            } else {
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())

        return lines.joinToString("") { line ->
            "<tspan x=\"0\" dy=\"1.2em\">$line</tspan>"
        }
    }
}