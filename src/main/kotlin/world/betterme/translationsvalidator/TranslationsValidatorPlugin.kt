package world.betterme.translationsvalidator

import org.gradle.api.Plugin
import org.gradle.api.Project

open class TranslationsValidatorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            "validator",
            TranslationsValidatorExtension::class.java,
            project
        )

        project.tasks.create("validateTranslations", ValidateTranslationsTask::class.java) {
            it.resourcesPath.set(extension.resourcesPath)
            it.slackWebHook.set(extension.slackWebHook)
        }
    }
}
