package world.betterme.translationsvalidator.core

import world.betterme.translationsvalidator.data.TranslationsLocalStore
import world.betterme.translationsvalidator.data.TranslationsLocalStoreImpl
import java.util.Locale

typealias Resources = Map<String, String>

data class ValidationError(val key: String, val locale: Locale, val type: IssueType)

sealed interface IssueType {
    data class Count(val expected: Int, val actual: Int) : IssueType
    data class Type(val position: Int, val expected: String, val actual: String) : IssueType
    data class Syntax(val placeholders: List<String>) : IssueType
}

class TranslationsValidator(
    private val store: TranslationsLocalStore,
    private val xmlParser: XmlParser,
    private val issuesReporter: IssuesReporter
) {

    fun validateAll() {
        println("Starting validation translations")
        val translations = mutableMapOf<Locale, Resources>()

        store.getFiles().forEach {
            val folderName = if (it.parentFile.name.contains("-")) {
                it.parentFile.name.substringAfter("-").ifEmpty { "en" }
            } else {
                "en"
            }
            val translationContent = it.readText()
            val parsedTranslationContent = xmlParser.parseXmlStrings(translationContent)
            translations[Locale.forLanguageTag(folderName)] = parsedTranslationContent
        }

        val referenceLocale = Locale("en")
        val referenceTranslation = translations[referenceLocale]
            ?: throw IllegalArgumentException("Reference locale $referenceLocale is missing.")

        val validationErrors = mutableListOf<ValidationError>()
        translations.forEach { (locale, translationStrings) ->
            translationStrings.forEach { (key, localizedText) ->
                val referenceText = referenceTranslation[key]
                if (referenceText != null) {
                    validatePlaceholders(
                        referenceText,
                        localizedText,
                        locale,
                        key,
                        validationErrors
                    )
                }
            }
        }

        issuesReporter.report(validationErrors)
    }

    private fun validatePlaceholders(
        referenceText: String,
        localizedText: String,
        locale: Locale,
        key: String,
        validationErrors: MutableList<ValidationError>
    ) {
        val referencePlaceholders = extractPlaceholdersFromText(referenceText)
        val localizedPlaceholders = extractPlaceholdersFromText(localizedText)

        // Validate placeholder count
        if (referencePlaceholders.size != localizedPlaceholders.size) {
            validationErrors.add(
                ValidationError(
                    key = key,
                    locale = locale,
                    type = IssueType.Count(
                        expected = referencePlaceholders.size,
                        actual = localizedPlaceholders.size
                    ),
                )
            )
        }

        // Validate placeholder types
        referencePlaceholders.forEachIndexed { index, referencePlaceholder ->
            val localizedPlaceholder = localizedPlaceholders.getOrNull(index) ?: return@forEachIndexed
            if (referencePlaceholder != localizedPlaceholder) {
                validationErrors.add(
                    ValidationError(
                        key = key,
                        locale = locale,
                        type = IssueType.Type(
                            expected = referencePlaceholder,
                            actual = localizedPlaceholder,
                            position = index
                        ),
                    )
                )
            }
        }

        // Validate placeholder syntax
        val incorrectPlaceholders = findIncorrectPlaceholders(localizedText)
        if (incorrectPlaceholders.isNotEmpty()) {
            validationErrors.add(
                ValidationError(
                    key = key,
                    locale = locale,
                    type = IssueType.Syntax(incorrectPlaceholders),
                )
            )
        }
    }

    /**
     * Extracts placeholders like %1$s, %1$d from the text.
     * Returns an ordered list of placeholders based on their index (e.g., %1$d, %2$s).
     * Non-positional placeholders (e.g., %s, %d) will be pushed to the end of the list.
     */
    private fun extractPlaceholdersFromText(text: String): List<String> {
        val placeholderRegex = """%\d+\$[a-zA-Z]|%[a-zA-Z]""".toRegex()
        return placeholderRegex.findAll(text)
            .map { it.value }
            .sortedBy { placeholder ->
                placeholder.substringAfter("%").substringBefore("$").toIntOrNull() ?: Int.MAX_VALUE
            }
            .toList()
    }

    private fun findIncorrectPlaceholders(text: String): List<String> {
        return incorrectPlaceholders.flatMap { pattern ->
            pattern.toRegex().findAll(text).mapNotNull {
                //exclude time formats
                if (!it.value.contains("%\\d\\d[s,d]".toRegex())) it.value else null
            }.toList()
        }
    }

    private val incorrectPlaceholders = listOf(
        """%\d+[s,d]""",           // %1s (no $)
        """%\d+\$\s+[s,d]""",      // %1$ s (space between $ and type)
        """%\d+\$\d+""",           // %1$2 (invalid position after $)
        """%[s, d]+\$""",          // %s$ (invalid at end)
        """%\s+[s, d] """,          // % s (space between % and type)
        """%\d+\s+\$\w""",         // %1 $s (space between number and $)
        """%\${'$'}s+""",          // %$s (invalid position)
        """ \${'$'}s+ """,         // $s (no %)
    )

    companion object Factory {

        @JvmStatic
        fun create(
            resourcesPath: String,
            shouldReportToSlack: Boolean,
            slackWebHook: String,
            reportPayload: String?
        ): TranslationsValidator {
            val store = TranslationsLocalStoreImpl(resourcesPath)
            val parser = XmlParser()
            val notifier = SlackNotifier(slackWebHook)
            val issuesReporter = IssuesReporter(notifier, shouldReportToSlack, reportPayload)
            return TranslationsValidator(
                store = store,
                xmlParser = parser,
                issuesReporter = issuesReporter,
            )
        }
    }
}
