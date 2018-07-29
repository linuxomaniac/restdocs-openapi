package cc.dille.restdocs.openapi

import org.amshove.kluent.shouldContain
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder


class OpenAPIResourceWriterTest {

    @Rule @JvmField val tempFolder = TemporaryFolder()

    @Test
    fun `should_write_map`() {
        tempFolder.newFile().let { file ->
            OpenAPIWriter.writeFile(file, mapOf("title" to "title", "baseUri" to "http://localhost"), "3.0.0")
            file.readLines().let {
                it.shouldContain("openapi: 3.0.0")
                it.shouldContain("title: title")
                it.shouldContain("baseUri: http://localhost")
            }
        }

    }
}
