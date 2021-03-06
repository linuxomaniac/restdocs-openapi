package cc.dille.restdocs.openapi.plugin.common

import org.amshove.kluent.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File


class OpenAPIFragmentTest : FragmentFixtures {

    @Rule
    @JvmField
    val testProjectDir = TemporaryFolder()

    private lateinit var file: File
    private lateinit var fragment: OpenAPIFragment
    private var expectedId = "some-get"


    @Test
    fun `should parse fragment from file`() {
        givenFile()

        whenFragmentReadFromFile()

        with(fragment) {
            id `should be equal to` expectedId
            path `should be equal to` "/carts/{cartId}"
            method.method `should be equal to` "get"
            method.requestContent?.contents?.shouldBeEmpty()
            method.responses `should contain` Response(status = 200, description = "TODO - figure out how to set")
            method.parameters `should contain` Parameter("cartId", "path", "some integer", true, "integer", "10")
        }
    }

    @Test
    fun `should parse fragment`() {
        whenFragmentReadFromMap(::rawFragment)

        with(fragment) {
            id `should be equal to` expectedId
            path `should be equal to` "/payment-integrations/{paymentIntegrationId}"
            method.method `should be equal to` "get"
            method.parameters `should contain` Parameter("paymentIntegrationId", "path", "The id", true, "integer", "12")
            method.requestContent?.contents?.shouldBeEmpty()
            method.responses `should contain` Response(
                    status = 200,
                    description = "some",
                    contents = listOf(Content(
                            contentType = "application/hal+json",
                            schema = Include("payment-integration-get-schema-response.json"),
                            example = Include("payment-integration-get-response.json")
                    ))
            )
        }
    }

    @Test
    fun `should parse fragment with example without schema`() {
        whenFragmentReadFromMap(this::rawFragmentWithoutSchema)

        with(fragment) {
            id `should be equal to` expectedId
            path `should be equal to` "/payment-integrations/{paymentIntegrationId}"
            method.method `should be equal to` "get"
            method.parameters `should contain` Parameter("paymentIntegrationId", "path", "The id", true, "integer", "12")
            method.requestContent?.contents?.shouldBeEmpty ()
            method.responses `should contain` Response(
                    status = 200,
                    description = "some description",
                    contents = listOf(Content(
                            contentType = "application/hal+json",
                            example = Include("payment-integration-get-response.json")
                    ))
            )
        }
    }

    @Test
    fun `should parse with empty response`() {
        whenFragmentReadFromMap(this::rawFragmentWithEmptyResponse)

        with(fragment) {
            id `should be equal to` expectedId
            path `should be equal to` "/payment-integrations/{paymentIntegrationId}"
            method.method `should be equal to` "get"
            method.requestContent?.contents?.shouldBeEmpty()
            method.parameters `should contain` Parameter("paymentIntegrationId", "path", "The id", true, "integer", "12")
            method.responses `should contain` Response(200, "some", emptyList(), listOf(ResponseHeader("Test-ResponseHeader", "hello")))
        }
    }

    @Test
    fun `should parse full fragment`() {
        whenFragmentReadFromMap(::rawFullFragment)

        with(fragment) {
            id `should equal` expectedId
            path `should equal` "/tags/{id}"

            with(method) {
                method `should equal` "put"
                operationId `should equal` "putTag"
                summary `should equal` "put-tag"

                parameters.size `should equal` 4

                parameters[0].name `should equal` "id"
                parameters[0].in_ `should equal` "path"
                parameters[0].description `should equal` "The id"
                parameters[0].required `should equal` true
                parameters[0].type `should equal` "integer"
                parameters[0].example `should equal` "12"

                parameters[1].name `should equal` "X-Custom-ResponseHeader"
                parameters[1].in_ `should equal` "header"
                parameters[1].description `should equal` "A custom header"
                parameters[1].required `should equal` true
                parameters[1].type `should equal` "string"
                parameters[1].example `should equal` "test"

                parameters[2].name `should equal` "some"
                parameters[2].in_ `should equal` "query"
                parameters[2].description `should equal` "some"
                parameters[2].required `should equal` false
                parameters[2].type `should equal` "integer"

                parameters[3].name `should equal` "other"
                parameters[3].in_ `should equal` "query"
                parameters[3].description `should equal` "other"
                parameters[3].required `should equal` true
                parameters[3].type `should equal` "string"
                parameters[3].example `should equal` "test"

                requestContent?.contents?.size `should equal` 1
                requestContent?.contents.`should not be null`()
                with(requestContent?.contents!!.first()) {
                    contentType `should equal` "application/hal+json"
                    schema.`should not be null`()
                    example.`should not be null`()
                }

                responses.size `should be` 1
                with(responses.first()) {
                    status `should equal` 200

                    headers.size `should be` 1
                    headers.first().name `should equal` "X-Custom-ResponseHeader"
                    headers.first().description `should equal` "A custom header"
                    headers.first().example `should equal` "test"

                    contents.first().example.`should not be null`()
                    contents.first().schema.`should not be null`()
                }
            }
        }
    }

    private fun whenFragmentReadFromMap(provider: () -> String) {
        fragment = OpenAPIFragment.fromYamlMap(expectedId, parsedFragmentMap(provider))
    }

    private fun whenFragmentReadFromFile() {
        fragment = OpenAPIFragment.fromFile(file)
    }

    private fun givenFile() {
        file = File(testProjectDir.newFolder("build", "generated-snippets", expectedId), "openapi-resource.yaml")
                .also {
                    it.writeText("""/carts/{cartId}:
                        |  get:
                        |    summary: gets a cart
                        |    operationId: getCart
                        |    parameters:
                        |      - name: cartId
                        |        in: path
                        |        description: some integer
                        |        required: true
                        |        schema:
                        |          type: integer
                        |        example: 10
                        |    responses:
                        |      200:
                        |        description: TODO - figure out how to set
    """.trimMargin())
                }
    }
}
