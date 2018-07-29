package cc.dille.restdocs.openapi

import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should exist`
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class RestdocsOpenAPIResourceTaskTest {

    @Rule
    @JvmField
    val testProjectDir = TemporaryFolder()

    lateinit var buildFile: File

    private var apiTitle = "API documentation"
    private var baseUri: String? = null
    private var openAPIVersion = "3.0.1"
    private var separatePublicApi: Boolean = false
    private var outputFileNamePrefix = "api"
    private lateinit var pluginClasspath: List<File>
    private lateinit var result: BuildResult

    @Before
    fun setUp() {
        buildFile = testProjectDir.newFile("build.gradle")

        testProjectDir.newFolder("build", "generated-snippets")

        pluginClasspath = javaClass.classLoader
                .getResourceAsStream("plugin-classpath.txt")
                ?.let { inputStream -> inputStream.reader().readLines().map { File(it) } }
                ?: throw IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
    }

    @Test
    fun `should aggregate openAPI fragments`() {
        apiTitle = "Notes API"
        baseUri = "http://localhost:8080/"
        separatePublicApi = true
        openAPIVersion = "3.0.1"
        outputFileNamePrefix = "index"
        givenBuildFileWithRamldocClosure()
        givenSnippetFiles()
        givenRequestBodyJsonFile()

        whenPluginExecuted()

        result.task(":openAPIdoc")?.outcome `should equal` SUCCESS
        thenApiRamlFileGenerated()
        thenGroupFileGenerated()
        thenRequestBodyJsonFileFoundInOutputDirectory()
    }

    @Test
    fun `should aggregate openAPI fragments with missing openAPIdoc closure`() {
        givenBuildFileWithoutRamldocClosure()
        givenSnippetFiles()
        givenRequestBodyJsonFile()

        whenPluginExecuted()

        result.task(":openAPIdoc")?.outcome `should equal` SUCCESS
        thenApiRamlFileGenerated()
        thenGroupFileGenerated()
        thenRequestBodyJsonFileFoundInOutputDirectory()
    }

    private fun thenRequestBodyJsonFileFoundInOutputDirectory() {
        File(testProjectDir.root, "build/openAPIdoc/carts-create-request.json").`should exist`()
    }

    private fun thenGroupFileGenerated() {
        val groupFile = File(testProjectDir.root, "build/openAPIdoc/carts.openapi")
        val groupFileLines = groupFile.readLines()
        println(groupFile.readText())
        groupFileLines.any { it.startsWith("get:") }.`should be true`()
        groupFileLines.any { it.startsWith("post:") }.`should be true`()
        groupFileLines.any { it.startsWith("/{cartId}:") }.`should be true`()
        groupFileLines.any { it.startsWith("  get:") }.`should be true`()
        groupFileLines.any { it.startsWith("  delete:") }.`should be true`()
        groupFileLines.any { it.contains("schema: \$ref: 'carts-create-request.json'") }.`should be true`()

        if (separatePublicApi)
            File(testProjectDir.root, "build/openAPIdoc/carts-public.openapi").`should exist`()
    }

    private fun thenApiRamlFileGenerated() {
        thenApiRamlFileExistsWithHeaders().also { lines ->
            lines `should contain` "/carts: !include 'carts.openapi'"
            lines `should contain` "/: !include 'root.openapi'"
        }
    }

    private fun thenApiRamlFileExistsWithHeaders(): List<String> {
        val apiFile = File(testProjectDir.root, "build/openAPIdoc/${outputFileNamePrefix}.openapi")
        apiFile.`should exist`()
        return apiFile.readLines().also { lines ->
            lines `should contain` "openapi: $openAPIVersion"
            lines `should contain` "title: $apiTitle"
            baseUri?.let { lines.any { it.startsWith("baseUri:") }.`should be true`() }
        }
    }

    private fun givenBuildFileWithoutRamldocClosure() {
        buildFile.writeText(baseBuildFile())
    }

    private fun givenBuildFileWithRamldocClosure() {
        buildFile.writeText(baseBuildFile() + """
openAPIdoc {
    apiTitle = '$apiTitle'
    apiBaseUri = '$baseUri'
    openAPIVersion = "$openAPIVersion"
    separatePublicApi = $separatePublicApi
    outputFileNamePrefix = "$outputFileNamePrefix"
}
""")
    }

    private fun whenPluginExecuted() {
        result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("--info", "--stacktrace", "openAPIdoc")
                .withPluginClasspath(pluginClasspath)
                .build()
    }

    private fun givenSnippetFiles() {
        File(testProjectDir.newFolder("build", "generated-snippets", "carts-create"), "openapi-resource.openapi").writeText("""/carts:
  post:
    description: "TODO - figure out how to set"
    securedBy: ["pymt:u"]
    body:
      application/hal+json:
        schema: !include carts-create-request.json
        example: !include carts-create-request.json
""")
        File(testProjectDir.newFolder("build", "generated-snippets", "carts-get"), "openapi-resource.openapi").writeText("""/carts/{cartId}:
  get:
    description: "TODO - figure out how to set"
    securedBy: ["pymt:u"]
    responses:
      200:
""")
        File(testProjectDir.newFolder("build", "generated-snippets", "carts-list"), "openapi-resource.openapi").writeText("""/carts:
  get:
    description: "TODO - figure out how to set"
    securedBy: ["pymt:u"]
""")
        File(testProjectDir.newFolder("build", "generated-snippets", "carts-delete"), "openapi-resource.openapi").writeText("""/carts/{cartId}:
  delete:
    description: "TODO - figure out how to set"
    securedBy: ["pymt:u"]
""")
        File(testProjectDir.newFolder("build", "generated-snippets", "index-get"), "openapi-resource.openapi").writeText("""/:
  get:
    description: "TODO - figure out how to set"
    securedBy: ["pymt:u"]
""")
    }

    private fun givenRequestBodyJsonFile() {
        testProjectDir.newFile("build/generated-snippets/carts-create/carts-create-request.json").writeText("""{}""")
    }

    private fun baseBuildFile() = """
buildscript {
    repositories {
        mavenCentral()
        maven { url "https://plugins.gradle.org/m2/" }
    }
}

plugins {
    id 'java'
    id 'com.epages.restdocs-openapi'
}
""".trimIndent()
}
