package world.betterme.translationsvalidator.data

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import world.betterme.translationsvalidator.core.SlackNotifier
import world.betterme.translationsvalidator.core.TranslationsValidator
import world.betterme.translationsvalidator.core.XmlParser
import java.io.File

class TranslationsValidatorTest {

    private val store = mockk<TranslationsLocalStore>()
    private val parser = XmlParser()
    private val notifier = mockk<SlackNotifier>(relaxed = true)

    @Test
    fun `validateAll should pass when all translations are valid`() {
        val parentPath = "src/test/resources/valid"
        val englishFile = File("$parentPath/value/strings.xml")
        val frenchFile = File("$parentPath/value-fr/strings.xml")

        every { store.getFiles() } returns listOf(englishFile, frenchFile)

        val validator = TranslationsValidator(
            store = store,
            xmlParser = parser,
            notifier = notifier,
            shouldReportToSlack = true
        )
        validator.validateAll()

        verify { store.getFiles() }
        verify(exactly = 0) { notifier.sendSlackMessage(any()) }
    }

    @Test
    fun `validateAll should report placeholder count mismatch`() {
        val parentPath = "src/test/resources/count_mismatch"
        val englishFile = File("$parentPath/value/strings.xml")
        val frenchFile = File("$parentPath/value-fr/strings.xml")

        every { store.getFiles() } returns listOf(englishFile, frenchFile)

        val validator = TranslationsValidator(store, parser, notifier, shouldReportToSlack = true)
        validator.validateAll()

        verify { store.getFiles() }
        verify { notifier.sendSlackMessage("Translation validation issues:\nPlaceholder count mismatch for key 'hello' in locale 'fr'. Expected 2, found 1.") }
    }

    @Test
    fun `validateAll should report placeholder type mismatch`() {
        val parentPath = "src/test/resources/type_mismatch"
        val englishFile = File("$parentPath/value/strings.xml")
        val frenchFile = File("$parentPath/value-fr/strings.xml")

        every { store.getFiles() } returns listOf(englishFile, frenchFile)

        val validator = TranslationsValidator(
            store = store,
            xmlParser = parser,
            notifier = notifier,
            shouldReportToSlack = true
        )
        validator.validateAll()

        verify { store.getFiles() }
        verify {
            notifier.sendSlackMessage(match {
                it == "Translation validation issues:\nPlaceholder type mismatch for key 'hello' in locale 'fr' at position 0. Expected %1\$s, found %1\$d."
            })
        }
    }

    @Test
    fun `validateAll correct compare placeholder when order mismatch`() {
        val parentPath = "src/test/resources/order_mismatch"
        val englishFile = File("$parentPath/value/strings.xml")
        val frenchFile = File("$parentPath/value-ar/strings.xml")

        every { store.getFiles() } returns listOf(englishFile, frenchFile)

        val validator = TranslationsValidator(
            store = store,
            xmlParser = parser,
            notifier = notifier,
            shouldReportToSlack = true
        )
        validator.validateAll()

        verify { store.getFiles() }
        verify(exactly = 0) { notifier.sendSlackMessage(any()) }
    }

    @Test
    fun `validateAll should not report to Slack if shouldReportToSlack is false`() {
        val parentPath = "src/test/resources/count_mismatch"
        val englishFile = File("$parentPath/value/strings.xml")
        val frenchFile = File("$parentPath/value-fr/strings.xml")

        every { store.getFiles() } returns listOf(englishFile, frenchFile)

        val validator = TranslationsValidator(
            store = store,
            xmlParser = parser,
            notifier = notifier,
            shouldReportToSlack = false
        )
        validator.validateAll()

        verify { store.getFiles() }
        verify(exactly = 0) { notifier.sendSlackMessage(any()) }
    }
}
