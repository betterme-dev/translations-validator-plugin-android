package world.betterme.translationsvalidator

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import world.betterme.translationsvalidator.core.TranslationsValidator

open class ValidateTranslationsTask : DefaultTask() {

    @Input
    val resourcesPath: Property<String> = project.objects.property(String::class.java)

    @Input
    val slackWebHook: Property<String> = project.objects.property(String::class.java)

    @Input
    @Optional
    val reportToSlack: Property<Boolean> = project.objects
        .property(Boolean::class.java)
        .convention(false)

    init {
        description =
            "Validates placeholders from translated strings.xml files by comparing them with main strings.xml file"
        group = "translations"
    }

    @Option(option = "reportToSlack", description = "Will send validation report to slack channel")
    fun setReportToSlack(reportToSlack: Boolean) {
        this.reportToSlack.set(reportToSlack)
    }

    @Suppress("unused")
    @TaskAction
    fun validatePlaceholders() {
        val translationsValidator = TranslationsValidator.create(
            resourcesPath = resourcesPath.get(),
            shouldReportToSlack = reportToSlack.get(),
            slackWebHook = slackWebHook.get()
        )
        translationsValidator.validateAll()
    }
}
