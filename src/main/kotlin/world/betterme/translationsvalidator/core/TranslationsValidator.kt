package world.betterme.translationsvalidator.core

import world.betterme.translationsvalidator.data.TranslationsLocalStore
import world.betterme.translationsvalidator.data.TranslationsLocalStoreImpl
import java.util.Locale

typealias Resources = Map<String, String>

class TranslationsValidator(
    private val store: TranslationsLocalStore,
    private val xmlParser: XmlParser,
    private val notifier: SlackNotifier,
    private val shouldReportToSlack: Boolean,
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

        val validationErrors = mutableListOf<String>()
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

        if (validationErrors.isNotEmpty() && shouldReportToSlack) {
            val report = validationErrors.joinToString(separator = "\n")
            notifier.sendSlackMessage("Translation validation issues:\n$report")
        } else {
            println("All translations validated successfully!")
        }
    }

    private fun validatePlaceholders(
        referenceText: String,
        localizedText: String,
        locale: Locale,
        key: String,
        validationErrors: MutableList<String>
    ) {
        val referencePlaceholders = extractPlaceholdersFromText(referenceText)
        val localizedPlaceholders = extractPlaceholdersFromText(localizedText)

        // Validate placeholder count
        if (referencePlaceholders.size != localizedPlaceholders.size) {
            validationErrors.add(
                "Placeholder count mismatch for key '$key' in locale '$locale'. " +
                        "Expected ${referencePlaceholders.size}, found ${localizedPlaceholders.size}."
            )
        }

        // Validate placeholder types
        referencePlaceholders.forEachIndexed { index, referencePlaceholder ->
            val localizedPlaceholder = localizedPlaceholders.getOrNull(index) ?: return@forEachIndexed
            if (referencePlaceholder != localizedPlaceholder) {
                validationErrors.add(
                    "Placeholder type mismatch for key '$key' in locale '$locale' at position $index. " +
                            "Expected $referencePlaceholder, found $localizedPlaceholder."
                )
            }
        }
    }

    /**
     * Extracts placeholders like %1$s, %1$d from the text.
     */
    private fun extractPlaceholdersFromText(text: String): List<String> {
        val placeholderRegex = """%\d+\$[a-zA-Z]|\%[a-zA-Z]""".toRegex()
        return placeholderRegex.findAll(text).map { it.value }.toList()
    }

    companion object Factory {

        @JvmStatic
        fun create(
            resourcesPath: String,
            shouldReportToSlack: Boolean,
            slackWebHook: String
        ): TranslationsValidator {
            val store = TranslationsLocalStoreImpl(resourcesPath)
            val parser = XmlParser()
            val notifier = SlackNotifier(slackWebHook)
            return TranslationsValidator(
                store = store,
                xmlParser = parser,
                notifier = notifier,
                shouldReportToSlack = shouldReportToSlack
            )
        }
    }
}
