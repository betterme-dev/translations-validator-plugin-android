package world.betterme.translationsvalidator

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class TranslationsValidatorPluginTest {

    @Test
    fun `plugin is applied successfully`() {
        val project = ProjectBuilder.builder().build()

        project.pluginManager.apply("world.betterme.translationsvalidator")

        assertNotNull(project.tasks.findByName("validateTranslations"))
    }
}
