package world.betterme.translationsvalidator.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.betterme.translationsvalidator.data.TranslationsLocalStoreImpl
import java.io.File

class TranslationsLocalStoreImplTest {

    @Test
    fun `getFiles should return empty list if folder does not exist`() {
        val resourcesFile = File("wrong_file_path")

        val store = TranslationsLocalStoreImpl(resourcesFile.absolutePath)
        val result = store.getFiles()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getFiles should return empty list if folder is not a directory`() {
        val resourcesFile = File("src/test/resources/valid/value/strings.xml")

        val store = TranslationsLocalStoreImpl(resourcesFile.absolutePath)
        val result = store.getFiles()

        assertTrue(result.isEmpty())
    }


    @Test
    fun `getFiles should return list of strings xml files`() {
        val resourcesFolder = File("src/test/resources/valid/")
        val store = TranslationsLocalStoreImpl(resourcesFolder.absolutePath)
        val result = store.getFiles()

        assertEquals(2, result.size)
        assertTrue(result[0].name == "strings.xml")
        assertTrue(result[1].name == "strings.xml")
    }
}

