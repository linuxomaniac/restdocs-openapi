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

class RestdocsOpenAPITaskTest {
    @Rule
    @JvmField
    val testProjectDir = TemporaryFolder()

    lateinit var buildFile: File

    private var apiTitle = "API documentation"
    private var baseUri: String? = null
    private var openAPIVersion = "3.0.1"
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
        openAPIVersion = "3.0.1"
        outputFileNamePrefix = "index"
        givenBuildFileWithOpenAPIDocClosure()
        givenSnippetFiles()
        givenRequestBodyJsonFile()

        whenPluginExecuted()

        result.task(":openAPIdoc")?.outcome `should equal` SUCCESS
        thenOpenAPIFileGenerated()
        thenGroupFileGenerated()
        thenRequestBodyJsonFileFoundInOutputDirectory()
    }

    @Test
    fun `should aggregate openAPI fragments with missing openAPIdoc closure`() {
        givenBuildFileWithoutOpenAPIDocClosure()
        givenSnippetFiles()
        givenRequestBodyJsonFile()

        whenPluginExecuted()

        result.task(":openAPIdoc")?.outcome `should equal` SUCCESS
        thenOpenAPIFileGenerated()
        thenGroupFileGenerated()
        thenRequestBodyJsonFileFoundInOutputDirectory()
    }

    private fun thenRequestBodyJsonFileFoundInOutputDirectory() {
        File(testProjectDir.root, "build/openAPIdoc/carts-create-request.json").`should exist`()
    }

    private fun thenGroupFileGenerated() {
        val groupFile = File(testProjectDir.root, "build/openAPIdoc/carts.yaml")
        val groupFileLines = groupFile.readLines()
        groupFileLines.any { it.startsWith("get:") }.`should be true`()
        groupFileLines.any { it.startsWith("post:") }.`should be true`()
        groupFileLines.any { it.startsWith("/{cartId}:") }.`should be true`()
        groupFileLines.any { it.startsWith("  get:") }.`should be true`()
        groupFileLines.any { it.startsWith("  delete:") }.`should be true`()
        groupFileLines.any { it.contains("schema: !include 'carts-create-request.json'") }.`should be true`()
    }

    private fun thenOpenAPIFileGenerated() {
        thenOpenAPIFileExistsWithHeaders().also { lines ->
            lines `should contain` "/carts: !include 'carts.yaml'"
            lines `should contain` "/: !include 'root.yaml'"
        }
    }

    private fun thenOpenAPIFileExistsWithHeaders(): List<String> {
        val apiFile = File(testProjectDir.root, "build/openAPIdoc/${outputFileNamePrefix}.yaml")
        apiFile.`should exist`()
        return apiFile.readLines().also { lines ->
            lines `should contain` "openapi: $openAPIVersion"
            lines `should contain` "title: $apiTitle"
            baseUri?.let { lines.any { it.startsWith("baseUri:") }.`should be true`() }
        }
    }

    private fun givenBuildFileWithoutOpenAPIDocClosure() {
        buildFile.writeText(baseBuildFile())
    }

    private fun givenBuildFileWithOpenAPIDocClosure() {
        buildFile.writeText(baseBuildFile() + """
openAPIdoc {
    apiTitle = '$apiTitle'
    apiBaseUri = '$baseUri'
    openAPIVersion = "$openAPIVersion"
    outputFileNamePrefix = "$outputFileNamePrefix"
}
""")
    }

    private fun whenPluginExecuted() {
        result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("--info", "--stacktrace", "openAPIdoc")
                .withPluginClasspath(pluginClasspath)
//                .forwardOutput()
                .build()
    }

    private fun givenSnippetFiles() {
        File(testProjectDir.newFolder("build", "generated-snippets", "carts-create"), "openapi-resource.yaml").writeText("""/carts:
  post:
    requestBody:
      required: true
      content:
        application/hal+json:
          schema: !include 'carts-create-request.json'
          examples:
            example0: !include 'carts-create-request.json'
""")

        File(testProjectDir.newFolder("build", "generated-snippets", "carts-get"), "openapi-resource.yaml").writeText("""/carts/{cartId}:
  get:
    responses:
      200:
        description: "TODO - figure out how to set"
""")
        File(testProjectDir.newFolder("build", "generated-snippets", "carts-list"), "openapi-resource.yaml").writeText("""/carts:
  get:
    responses:
      200:
        description: "TODO - figure out how to set"
""")
        File(testProjectDir.newFolder("build", "generated-snippets", "carts-delete"), "openapi-resource.yaml").writeText("""/carts/{cartId}:
  delete:
    responses:
      201:
        description: "TODO - figure out how to set"
""")
        File(testProjectDir.newFolder("build", "generated-snippets", "index-get"), "openapi-resource.yaml").writeText("""/:
  get:
    responses:
      200:
        description: "TODO - figure out how to set"
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
    id 'cc.dille.restdocs-openapi'
}
""".trimIndent()
}
