package cc.dille.restdocs.openapi.plugin.common

import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should exist`
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File


abstract class RestdocsOpenAPITaskTestResources {
    @Rule
    @JvmField
    val testProjectDir = TemporaryFolder()

    lateinit var buildFile: File

    // These are the default values in RestdocsOpenAPIGradlePluginExtension
    protected var openAPIVersion = "3.0.1"
    protected var infoVersion = "0.1.0"
    protected var infoTitle = "API documentation"
    protected var infoDescription: String? = null
    protected var infoContactName: String? = null
    protected var infoContactEmail: String? = null
    protected var infoContactUrl: String? = null
    protected var serverUrl: String? = null
    protected var serverDescription: String? = null
    protected var outputFileNamePrefix = "api"

    protected lateinit var pluginClasspath: List<File>


    @Before
    fun setUp() {
        pluginClasspath = javaClass.classLoader
                .getResourceAsStream("plugin-classpath.txt")
                ?.let { inputStream -> inputStream.reader().readLines().map { File(it) } }
                ?: throw IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
    }

    fun givenVariablesDefinition() {
        openAPIVersion = "3.0.0"
        infoVersion = "0.1.0-test"
        infoTitle = "API documentation test"
        infoDescription = "I hope the tests are OK"
        infoContactName = "John Smith"
        infoContactEmail = "abuse@example.org"
        infoContactUrl = "http://example.org"
        serverUrl = "example.org"
        serverDescription = "Default server"
        outputFileNamePrefix = "index"
    }

    protected fun thenGroupFileGenerated() {
        thenOpenAPIFileExistsWithHeaders().also { apiFile ->

            apiFile.readLines().apply {
                any { it.startsWith("  /:") }.`should be true`()
                any { it.startsWith("  /carts/{cartId}:") }.`should be true`()
                any { it.startsWith("  /carts:") }.`should be true`()
                any { it.startsWith("    get:") }.`should be true`()
                any { it.startsWith("    post:") }.`should be true`()
                any { it.startsWith("    delete:") }.`should be true`()
                any { it.startsWith("      summary: post cart") }.`should be true`()
                any { it.startsWith("      summary: delete cart") }.`should be true`()
                any { it.startsWith("      summary: cart get") }.`should be true`()
                any { it.startsWith("      summary: get index") }.`should be true`()
                any { it.startsWith("      summary: carts listing") }.`should be true`()
            }

            apiFile.readText().replace("\\s+".toRegex(), " ").apply {
                contains("example: " + requestBodyJson()).`should be true`()
                contains("- name: some in: query description: some required: false schema: type: integer").`should be true`()
                contains("- name: other in: query description: other required: true schema: type: string example: test").`should be true`()
                contains("deleteSelf: operationId: deleteCart description: deletes self parameters: cartId: ${'$'}response.body#/id").`should be true`()
                contains("getSelf: operationId: getCart description: gets self parameters: cartId: ${'$'}response.body#/id").`should be true`()
                contains("headers: Why-Not: description: how are you").`should be true`()
            }
        }
    }

    protected fun thenOpenAPIFileExistsWithHeaders(): File {
        val apiFile = File(testProjectDir.root, "build/openAPIdoc/${outputFileNamePrefix}.yaml")
        apiFile.`should exist`()
        apiFile.readLines().also { lines ->
            lines.any { it.contains("openapi: $openAPIVersion") }.`should be true`()
            lines.any { it.contains("version: $infoVersion") }.`should be true`()
            lines.any { it.contains("title: $infoTitle") }.`should be true`()
            infoContactName?.let { _ -> lines.any { it.contains("name: $infoContactName") }.`should be true`() }
            infoContactEmail?.let { _ -> lines.any { it.contains("email: $infoContactEmail") }.`should be true`() }
            infoContactUrl?.let { _ -> lines.any { it.contains("url: $infoContactUrl") }.`should be true`() }
            infoDescription?.let { _ -> lines.any { it.contains("description: $infoDescription") }.`should be true`() }
            serverUrl?.let { _ -> lines.any { it.contains("url: $serverUrl") }.`should be true`() }
            serverDescription?.let { _ -> lines.any { it.contains("description: $serverDescription") }.`should be true`() }
        }

        return apiFile
    }

    protected fun givenSnippetFiles() {
        File(testProjectDir.newFolder("build", "generated-snippets", "carts-create"), "openapi-resource.yaml").writeText("""/carts:
  post:
    summary: post cart
    requestBody:
      required: true
      content:
        application/hal+json:
          example: !include 'carts-create-request.json'
    responses:
      201:
        description: Cart created
        links:
          getSelf:
            operationId: getCart
            description: gets self
            parameters:
              cartId: ${'$'}response.body#/id
          deleteSelf:
            operationId: deleteCart
            description: deletes self
            parameters:
              cartId: ${'$'}response.body#/id
""")

        File(testProjectDir.newFolder("build", "generated-snippets", "carts-get"), "openapi-resource.yaml").writeText("""/carts/{cartId}:
  get:
    summary: cart get
    operationId: getCart
    responses:
      200:
        description: "TODO - figure out how to set"
""")
        File(testProjectDir.newFolder("build", "generated-snippets", "carts-list"), "openapi-resource.yaml").writeText("""/carts:
  get:
    summary: carts listing
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
    summary: delete cart
    operationId: deleteCart
    responses:
      201:
        description: "TODO - figure out how to set"
""")
        File(testProjectDir.newFolder("build", "generated-snippets", "index-get"), "openapi-resource.yaml").writeText("""/:
  get:
    summary: get index
    responses:
      200:
        description: "TODO - figure out how to set"
        headers:
          Why-Not:
            description: how are you
""")
    }

    protected fun requestBodyJson() = """{}"""

    protected fun givenRequestBodyJsonFile() {
        testProjectDir.newFile("build/generated-snippets/carts-create/carts-create-request.json").writeText(requestBodyJson())
    }

    protected fun givenBuildFileWithoutOpenAPIDocClosure() {
        buildFile.writeText(baseBuildFile())
    }

    protected fun givenBuildFileWithOpenAPIDocClosure() {
        buildFile.writeText(baseBuildFile() + additionsToBuildFile())
    }

    // Declaration of what test classes should implement

    protected abstract fun thenTaskSucceeded(): Boolean

    protected abstract fun whenPluginExecuted()

    protected abstract fun baseBuildFile() : String

    protected abstract fun additionsToBuildFile() : String

    fun `should aggregate openAPI fragments`() {
        givenVariablesDefinition()

        givenBuildFileWithOpenAPIDocClosure()
        givenSnippetFiles()
        givenRequestBodyJsonFile()

        whenPluginExecuted()

        thenTaskSucceeded().`should be true`()
        thenGroupFileGenerated()
    }


    fun `should aggregate openAPI fragments with missing openAPIdoc closure`() {
        givenBuildFileWithoutOpenAPIDocClosure()
        givenSnippetFiles()
        givenRequestBodyJsonFile()

        whenPluginExecuted()

        thenTaskSucceeded().`should be true`()
        thenGroupFileGenerated()
    }
}
