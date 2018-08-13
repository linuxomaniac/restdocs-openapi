package cc.dille.restdocs.openapi.plugin.common

import org.amshove.kluent.shouldContain
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder


class OpenAPIWriterTest {

    @Rule @JvmField val tempFolder = TemporaryFolder()

    @Test
    fun `should_write_map`() {
        tempFolder.newFile().let { file ->
            OpenAPIWriter.writeFile(file, mapOf("openapi" to "3.0.0", "title" to "title", "baseUri" to "http://localhost"))
            file.readLines().let {
                it.shouldContain("openapi: 3.0.0")
                it.shouldContain("title: title")
                it.shouldContain("baseUri: http://localhost")
            }
        }

    }
}
