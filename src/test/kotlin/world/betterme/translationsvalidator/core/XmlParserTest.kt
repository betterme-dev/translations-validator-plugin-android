package world.betterme.translationsvalidator.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class XmlParserTest {

    private val xmlParser = XmlParser()

    @Test
    fun `parseXmlStrings should parse valid XML with one string`() {
        val xmlContent = "<resources>" +
                "<string name=\"hello\">Hello %1\$s</string>" +
                "</resources>"

        val result = xmlParser.parseXmlStrings(xmlContent)

        assertEquals(1, result.size)
        assertEquals("Hello %1\$s", result["hello"])
    }

    @Test
    fun `parseXmlStrings should parse valid XML with multiple strings`() {
        val xmlContent = "<resources>" +
                "<string name=\"hello\">Hello %1\$s</string>" +
                "<string name=\"goodbye\">Goodbye %1\$s</string>" +
                "</resources>"

        val result = xmlParser.parseXmlStrings(xmlContent)

        assertEquals(2, result.size)
        assertEquals("Hello %1\$s", result["hello"])
        assertEquals("Goodbye %1\$s", result["goodbye"])
    }

    @Test
    fun `parseXmlStrings should return empty map for empty XML`() {
        val xmlContent = "<resources></resources>"

        val result = xmlParser.parseXmlStrings(xmlContent)

        assertEquals(0, result.size)
    }

    @Test
    fun `parseXmlStrings should handle XML with no strings`() {
        val xmlContent = """
            <resources>
                <!-- No string elements -->
            </resources>
        """.trimIndent()

        val result = xmlParser.parseXmlStrings(xmlContent)

        assertEquals(0, result.size)
    }

    @Test
    fun `parseXmlStrings should throw exception on invalid XML`() {
        val invalidXmlContent = "<resources><string name='hello'>Hello"

        val exception = org.junit.jupiter.api.assertThrows<Exception> {
            xmlParser.parseXmlStrings(invalidXmlContent)
        }
        assertEquals(
            "XML document structures must start and end within the same entity.",
            exception.message
        )
    }
}
