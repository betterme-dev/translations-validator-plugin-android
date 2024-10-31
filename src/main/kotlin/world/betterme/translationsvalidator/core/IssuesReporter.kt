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
            appendLine("Placeholder validation issues for translation keys:")
            appendLine()

            // Grouping errors by keys
            val groupedErrors = validationErrors.groupBy { it.key }

            for ((key, errors) in groupedErrors) {
                appendLine("`$key`:")

                // Grouping errors by issue type
                val issuesByType = errors.groupBy { it.type.id }
                val localeMap = mutableMapOf<Int, MutableList<String>>()

                // Collect and report syntax issues
                val syntaxIssues = issuesByType[IssueType.Syntax.ID]
                if (!syntaxIssues.isNullOrEmpty()) {
                    val placeholders = syntaxIssues.flatMap {
                        (it.type as IssueType.Syntax).placeholders
                    }.distinct()

                    // Collecting locales for syntax issues
                    syntaxIssues.forEach {
                        localeMap
                            .getOrPut(IssueType.Syntax.ID) { mutableListOf() }
                            .add("`${it.locale}`")
                    }

                    val placeholdersString = placeholders.joinToString(", ")
                    val localesString = localeMap[IssueType.Syntax.ID]?.distinct()?.joinToString(", ").orEmpty()
                    appendLine("    - syntax error: $placeholdersString in locales:")
                    appendLine("      $localesString")
                }

                // Collect and report type mismatches
                val typeMismatches = issuesByType[IssueType.Type.ID]
                if (!typeMismatches.isNullOrEmpty()) {
                    // Aggregate unique type mismatches by position and expected/actual values
                    val typeMismatchMap = mutableMapOf<String, MutableList<String>>()
                    typeMismatches.forEach {
                        val typeMismatch = it.type as IssueType.Type
                        val mismatchKey = "${typeMismatch.position}_${typeMismatch.expected}_${typeMismatch.actual}"
                        typeMismatchMap
                            .getOrPut(mismatchKey) { mutableListOf() }
                            .add("`${it.locale}`")
                    }

                    typeMismatchMap.forEach { (key, locales) ->
                        val (position, expected, actual) = key.split("_")
                        val localesString = locales.distinct().joinToString(", ")
                        appendLine("    - type mismatch at position $position. Expected $expected, found $actual in locales:")
                        appendLine("      $localesString")
                    }
                }

                // Collect and report count mismatches
                val countMismatches = issuesByType[IssueType.Count.ID]
                if (!countMismatches.isNullOrEmpty()) {
                    // Aggregate unique count mismatches by expected and actual values
                    val countMismatchMap = mutableMapOf<String, MutableList<String>>()
                    countMismatches.forEach {
                        val countMismatch = it.type as IssueType.Count
                        val mismatchKey = "${countMismatch.expected}_${countMismatch.actual}"
                        countMismatchMap
                            .getOrPut(mismatchKey) { mutableListOf() }
                            .add("`${it.locale}`")
                    }

                    countMismatchMap.forEach { (key, locales) ->
                        val (expected, actual) = key.split("_")
                        val localesString = locales.distinct().joinToString(", ")
                        appendLine("    - count mismatch. Expected $expected, found $actual in locales:")
                        appendLine("      $localesString")
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