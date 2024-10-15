package world.betterme.translationsvalidator

import org.gradle.api.Project
import org.gradle.api.provider.Property

open class TranslationsValidatorExtension(project: Project) {
    val resourcesPath: Property<String> = project.objects.property(String::class.java)
    val reportToSlack: Property<String> = project.objects.property(String::class.java)
    val slackWebHook: Property<String> = project.objects.property(String::class.java)
}