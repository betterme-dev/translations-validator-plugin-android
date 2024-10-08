package world.betterme.translationsvalidator.data

import java.io.File

interface TranslationsLocalStore {

    fun getFiles(): List<File>

}

internal class TranslationsLocalStoreImpl(
    private val resFolderPath: String,
) : TranslationsLocalStore {

    override fun getFiles(): List<File> {
        val resFolder = File(resFolderPath)

        // Ensure the resource folder exists
        if (!resFolder.exists() || !resFolder.isDirectory) {
            return emptyList()
        }

        // Recursively find all `strings.xml` files
        return resFolder.walkTopDown().filter { it.isFile && it.name == "strings.xml" }.toList()
    }
}
