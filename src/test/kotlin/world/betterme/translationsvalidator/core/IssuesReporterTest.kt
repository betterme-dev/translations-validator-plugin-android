package world.betterme.translationsvalidator.core

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.Locale

class IssuesReporterTest {

    private val notifier = mockk<SlackNotifier>(relaxed = true)

    @Test
    fun `should correct build and send report`() {
        val issuesReporter = IssuesReporter(
            notifier = notifier,
            shouldReportToSlack = true,
            reportPayload = "Author: AMayst"
        )
        val errors = listOf(
            ValidationError(
                key = "key1",
                locale = Locale.ENGLISH,
                type = IssueType.Count(2, 1)
            ),
            ValidationError(
                key = "key2",
                locale = Locale.ENGLISH,
                type = IssueType.Type(0, "%s", "%d")
            ),
            ValidationError(
                key = "key3",
                locale = Locale.ENGLISH,
                type = IssueType.Syntax(listOf("\$s", "%1 \$s"))
            )
        )

        issuesReporter.report(errors)

        val expectedReport = "Placeholder validation issues for translation keys:\n" +
                "\n" +
                "`key1`:\n" +
                "    - count mismatch. Expected 2, found 1 in locales:\n" +
                "      `en`\n" +
                "\n" +
                "`key2`:\n" +
                "    - type mismatch at position 0. Expected %s, found %d in locales:\n" +
                "      `en`\n" +
                "\n" +
                "`key3`:\n" +
                "    - syntax error: \$s, %1 \$s in locales:\n" +
                "      `en`\n" +
                "\n" +
                "Author: AMayst\n"

        verify { notifier.sendSlackMessage(expectedReport) }
    }

    @Test
    fun `should not send report if shouldReportToSlack false`() {
        val issuesReporter = IssuesReporter(
            notifier = notifier,
            shouldReportToSlack = false,
            reportPayload = null
        )
        val errors = listOf(
            ValidationError(
                key = "key1",
                locale = Locale.ENGLISH,
                type = IssueType.Count(2, 1)
            )
        )

        issuesReporter.report(errors)

        verify(exactly = 0) { notifier.sendSlackMessage(any()) }
    }

}
