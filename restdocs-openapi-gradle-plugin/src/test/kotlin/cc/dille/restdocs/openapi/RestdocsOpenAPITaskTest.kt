package cc.dille.restdocs.openapi

import org.amshove.kluent.*
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

    // These are the default values in RestdocsOpenAPIPluginExtension
    private var openAPIVersion = "3.0.1"
    private var infoVersion = "0.1.0"
    private var infoTitle = "API documentation"
    private var infoDescription: String? = null
    private var infoContactName: String? = null
    private var infoContactEmail: String? = null
    private var serverUrl: String? = null
    private var serverDescription: String? = null
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
        openAPIVersion = "3.0.0"
        infoVersion = "0.1.0-test"
        infoTitle = "API documentation test"
        infoDescription = "I hope the tests are OK"
        infoContactName = "John Smith"
        infoContactEmail = "abuse@example.org"
        serverUrl = "example.org"
        serverDescription = "Default server"
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
        val groupFileContent = groupFile.readText().replace("\\s+".toRegex(), " ")

        groupFileLines.any { it.startsWith("get:") }.`should be true`()
        groupFileLines.any { it.startsWith("post:") }.`should be true`()
        groupFileLines.any { it.startsWith("/{cartId}:") }.`should be true`()
        groupFileLines.any { it.startsWith("  get:") }.`should be true`()
        groupFileLines.any { it.startsWith("  delete:") }.`should be true`()
        groupFileLines.any { it.contains("schema: !include 'carts-create-request-schema.json'") }.`should be true`()

        groupFileContent.contains("examples: example0: !include 'carts-create-request.json'").`should be true`()
        groupFileContent.contains("- name: some in: query description: some required: false schema: type: integer").`should be true`()
        groupFileContent.contains("- name: other in: query description: other required: true schema: type: string example: test").`should be true`()
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
            lines.any { it.contains("openapi: $openAPIVersion") }.`should be true`()
            lines.any { it.contains("version: $infoVersion") }.`should be true`()
            lines.any { it.contains("title: $infoTitle") }.`should be true`()
            infoContactName?.let { _ -> lines.any { it.contains("name: $infoContactName") }.`should be true`() }
            infoContactEmail?.let { _ -> lines.any { it.contains("email: $infoContactEmail") }.`should be true`() }
            infoDescription?.let { _ -> lines.any { it.contains("description: $infoDescription") }.`should be true`() }
            serverUrl?.let { _ -> lines.any { it.contains("url: $serverUrl") }.`should be true`() }
            serverDescription?.let { _ -> lines.any { it.contains("description: $serverDescription") }.`should be true`() }
        }
    }

    private fun givenBuildFileWithoutOpenAPIDocClosure() {
        buildFile.writeText(baseBuildFile())
    }

    private fun givenBuildFileWithOpenAPIDocClosure() {
        buildFile.writeText(baseBuildFile() + """
openAPIdoc {
    openAPIVersion = "$openAPIVersion"
    infoVersion = "$infoVersion"
    infoTitle = "$infoTitle"
    infoDescription = "$infoDescription"
    infoContactName = "$infoContactName"
    infoContactEmail = "$infoContactEmail"
    serverUrl = "$serverUrl"
    serverDescription = "$serverDescription"
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
          schema: !include 'carts-create-request-schema.json'
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
    parameters:
      - name: some
        in: query
        description: some
        required: false
        schema:
          type: integer
      - name: other
        in: query
        description: other
        required: true
        schema:
          type: string
        example: test
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
