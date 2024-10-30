package world.betterme.translationsvalidator.core

class IssuesReporter(
    private val notifier: SlackNotifier,
    private val shouldReportToSlack: Boolean,
    private val reportPayload: String?
) {
    fun report(validationErrors: List<ValidationError>) {
        if (validationErrors.isNotEmpty()) {
            val report = buildReport(validationErrors)
            println(report)
            if (shouldReportToSlack) notifier.sendSlackMessage(report)
        } else {
            println("All translations validated successfully!")
        }
    }

    private fun buildReport(
        validationErrors: List<ValidationError>,
    ): String {
        return buildString {
            appendLine("Translation validation issues:")
            appendLine()

            // Grouping errors by keys and locales
            val groupedErrors = validationErrors.groupBy { it.key }

            for ((key, errors) in groupedErrors) {
                appendLine("Key `$key` issues:")

                // Grouping errors by locale
                val errorsByLocale = errors.groupBy { it.locale }
                for ((locale, localeErrors) in errorsByLocale) {
                    appendLine("  Locale: `$locale`")

                    // Collecting and categorizing error messages
                    val syntaxIssues = localeErrors.filter { it.type is IssueType.Syntax }
                    val typeMismatches = localeErrors.filter { it.type is IssueType.Type }
                    val countMismatches = localeErrors.filter { it.type is IssueType.Count }

                    // Reporting syntax issues
                    if (syntaxIssues.isNotEmpty()) {
                        val placeholders = syntaxIssues.flatMap {
                            (it.type as IssueType.Syntax).placeholders
                        }
                        appendLine(
                            "    - syntax issues with placeholders: ${
                                placeholders.joinToString(
                                    ", "
                                )
                            }"
                        )
                    }

                    // Reporting type mismatches
                    for (typeMismatch in typeMismatches) {
                        val errorType = typeMismatch.type as IssueType.Type
                        appendLine("    - type mismatch at position ${errorType.position}. Expected ${errorType.expected}, found ${errorType.actual}.")
                    }

                    // Reporting count mismatches
                    for (countMismatch in countMismatches) {
                        val errorType = countMismatch.type as IssueType.Count
                        appendLine("    - count mismatch. Expected ${errorType.expected} placeholders, found ${errorType.actual}.")
                    }
                }

                appendLine()
            }

            // Including additional report payload if provided
            if (!reportPayload.isNullOrEmpty()) {
                appendLine(reportPayload)
            }
        }
    }

}