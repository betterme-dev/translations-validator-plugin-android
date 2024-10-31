package world.betterme.translationsvalidator.data

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import world.betterme.translationsvalidator.core.IssuesReporter
import world.betterme.translationsvalidator.core.SlackNotifier
import world.betterme.translationsvalidator.core.TranslationsValidator
import world.betterme.translationsvalidator.core.XmlParser
import java.io.File

class TranslationsValidatorTest {

    private val store = mockk<TranslationsLocalStore>()
    private val parser = XmlParser()
    private val notifier = mockk<SlackNotifier>(relaxed = true)
    private val issuesReporter = IssuesReporter(
        notifier = notifier,
        shouldReportToSlack = true,
        reportPayload = "PR: Validation plugin \nAuthor: AMayst"
    )

    @Test
    fun `validateAll should pass when all translations are valid`() {
        val parentPath = "src/test/resources/valid"
        val englishFile = File("$parentPath/value/strings.xml")
        val frenchFile = File("$parentPath/value-fr/strings.xml")

        every { store.getFiles() } returns listOf(englishFile, frenchFile)

        val validator = TranslationsValidator(
            store = store,
            xmlParser = parser,
            issuesReporter = issuesReporter,
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

        val validator = TranslationsValidator(
            store = store,
            xmlParser = parser,
            issuesReporter = issuesReporter
        )
        validator.validateAll()

        verify { store.getFiles() }
        verify {
            notifier.sendSlackMessage(
                "Translation validation issues:\n" +
                        "\n" +
                        "Key `hello` issues:\n" +
                        "    - count mismatch. Expected 2 placeholders, found 1 in locales:\n" +
                        "      `fr`\n" +
                        "\n" +
                        "PR: Validation plugin \n" +
                        "Author: AMayst\n"
            )
        }
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
            issuesReporter = issuesReporter
        )
        validator.validateAll()

        verify { store.getFiles() }
        verify {
            notifier.sendSlackMessage(match {
                it == "Translation validation issues:\n" +
                        "\n" +
                        "Key `hello` issues:\n" +
                        "    - type mismatch at position 0. Expected %1\$s, found %1\$d in locales:\n" +
                        "      `fr`\n" +
                        "\n" +
                        "PR: Validation plugin \n" +
                        "Author: AMayst\n"
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
            issuesReporter = issuesReporter
        )
        validator.validateAll()

        verify { store.getFiles() }
        verify(exactly = 0) { notifier.sendSlackMessage(any()) }
    }

    @Test
    fun `validateAll should report placeholder syntax issues`() {
        val parentPath = "src/test/resources/syntax_issues"
        val englishFile = File("$parentPath/value/strings.xml")
        val frenchFile = File("$parentPath/value-fr/strings.xml")

        every { store.getFiles() } returns listOf(englishFile, frenchFile)

        val validator = TranslationsValidator(
            store = store,
            xmlParser = parser,
            issuesReporter = issuesReporter
        )
        validator.validateAll()

        verify { store.getFiles() }
        verify {
            notifier.sendSlackMessage(match {
                it == "Translation validation issues:\n" +
                        "\n" +
                        "Key `error1` issues:\n" +
                        "    - syntax issues with placeholder: %3s in locales:\n" +
                        "      `en`, `fr`\n" +
                        "\n" +
                        "Key `error2` issues:\n" +
                        "    - syntax issues with placeholder: %1\$ s in locales:\n" +
                        "      `en`, `fr`\n" +
                        "\n" +
                        "Key `error3` issues:\n" +
                        "    - syntax issues with placeholder: %s\$ in locales:\n" +
                        "      `en`, `fr`\n" +
                        "\n" +
                        "Key `error5` issues:\n" +
                        "    - syntax issues with placeholder: %1 \$s in locales:\n" +
                        "      `en`, `fr`\n" +
                        "\n" +
                        "Key `error6` issues:\n" +
                        "    - syntax issues with placeholder: %\$s in locales:\n" +
                        "      `en`, `fr`\n" +
                        "\n" +
                        "Key `error7` issues:\n" +
                        "    - syntax issues with placeholder: %\$s in locales:\n" +
                        "      `en`, `fr`\n" +
                        "\n" +
                        "Key `error8` issues:\n" +
                        "    - syntax issues with placeholder: %3s in locales:\n" +
                        "      `en`\n" +
                        "\n" +
                        "Key `error9` issues:\n" +
                        "    - syntax issues with placeholder: %1\$ s in locales:\n" +
                        "      `en`\n" +
                        "\n" +
                        "Key `error10` issues:\n" +
                        "    - syntax issues with placeholder: %1\$2 in locales:\n" +
                        "      `en`\n" +
                        "\n" +
                        "Key `error11` issues:\n" +
                        "    - syntax issues with placeholder: %s\$ in locales:\n" +
                        "      `en`\n" +
                        "\n" +
                        "Key `error12` issues:\n" +
                        "    - syntax issues with placeholder: % s  in locales:\n" +
                        "      `en`\n" +
                        "\n" +
                        "Key `error13` issues:\n" +
                        "    - syntax issues with placeholder: %1 \$s,  \$s  in locales:\n" +
                        "      `en`\n" +
                        "\n" +
                        "Key `error14` issues:\n" +
                        "    - syntax issues with placeholder: %\$s in locales:\n" +
                        "      `en`\n" +
                        "\n" +
                        "Key `error15` issues:\n" +
                        "    - syntax issues with placeholder: %\$s in locales:\n" +
                        "      `en`\n" +
                        "\n" +
                        "PR: Validation plugin \n" +
                        "Author: AMayst\n"
            })
        }
    }
}
