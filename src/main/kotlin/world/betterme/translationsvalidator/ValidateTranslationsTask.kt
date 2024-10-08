package world.betterme.translationsvalidator

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import world.betterme.translationsvalidator.core.TranslationsValidator

open class ValidateTranslationsTask : DefaultTask() {

    @Input
    val resourcesPath: Property<String> = project.objects.property(String::class.java)

    @Input
    val slackWebHook: Property<String> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val reportToSlack: Property<String>? = project.objects.property(String::class.java)

    init {
        description =
            "Validates placeholders from translated strings.xml files by comparing them with main strings.xml file"
        group = "translations"
    }

    @Suppress("unused")
    @TaskAction
    fun validatePlaceholders() {
        val translationsValidator = TranslationsValidator.create(
            resourcesPath = resourcesPath.get(),
            shouldReportToSlack = reportToSlack?.get()?.toBoolean() ?: false,
            slackWebHook = slackWebHook.get()
        )
        translationsValidator.validateAll()
    }
}
