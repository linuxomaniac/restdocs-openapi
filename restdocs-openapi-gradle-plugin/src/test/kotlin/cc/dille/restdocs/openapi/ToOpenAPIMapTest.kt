package cc.dille.restdocs.openapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.jayway.jsonpath.JsonPath
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should not be empty`
import org.junit.Test


class ToOpenAPIMapTest: FragmentFixtures {

    val objectMapper = ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)

    @Test
    fun `should convert minimal resource to openAPI map`() {
        val fragments = listOf(
                OpenAPIFragment("cart-line-item-update", "/carts/{id}",
                        Method(method = "put")
                ),
                OpenAPIFragment("cart-get", "/carts/{id}",
                        Method(method = "get")
                )
        )

        val openAPIMap = OpenAPI.fromFragments(fragments, NoOpJsonSchemaMerger).toOpenAPIMap(OpenAPIVersion.V_3_0_1)

        with (JsonPath.parse(objectMapper.writeValueAsString(openAPIMap))) {
            read<String>("/carts/{id}.get.description").`should not be empty`()
            read<String>("/carts/{id}.put.description").`should not be empty`()
        }
    }

    @Test
    fun `should convert full resource to openAPI map`() {
        val fragments = listOf(OpenAPIFragment.fromYamlMap("some", parsedFragmentMap { rawFullFragment() }))

        val openAPIMap = OpenAPI.fromFragments(fragments, NoOpJsonSchemaMerger).toOpenAPIMap(OpenAPIVersion.V_3_0_1)

        with (JsonPath.parse(objectMapper.writeValueAsString(openAPIMap))) {
            read<List<Parameter>>("/tags/{id}.parameters").size `should be equal to` 4
            read<List<Parameter>>("/tags/{id}.parameters").forEach {
                with(it) {
                    name.`should not be empty`()
                    in_.`should not be empty`()
                    description.`should not be empty`()
                    type.`should not be empty`()
                    example.`should not be empty`()
                }
            }
            read<String>("/tags/{id}.put.requestBody.content.application/hal+json.schema.\$ref").`should not be empty`()
            read<String>("/tags/{id}.put.requestBody.content.application/hal+json.examples.example0.\$ref").`should not be empty`()
            read<String>("/tags/{id}.put.responses.200.headers.X-Custom-Header.description").`should not be empty`()
            read<String>("/tags/{id}.put.responses.200.headers.X-Custom-Header.example").`should not be empty`()
            read<String>("/tags/{id}.put.responses.200.content.application/hal+json.schema.\$ref").`should not be empty`()
            read<String>("/tags/{id}.put.responses.200.content.application/hal+json.examples.example0.\$ref").`should not be empty`()
        }
    }

    @Test
    fun `should convert resource with empty response`() {
        val fragments = listOf(OpenAPIFragment.fromYamlMap("some", parsedFragmentMap { rawFragmentWithEmptyResponse() }))

        val openAPIMap = OpenAPI.fromFragments(fragments, NoOpJsonSchemaMerger).toOpenAPIMap(OpenAPIVersion.V_3_0_1)

        with (JsonPath.parse(objectMapper.writeValueAsString(openAPIMap))) {
            read<Map<String, *>>("/payment-integrations/{paymentIntegrationId}.get.responses.200").`should not be empty`()
            read<String>("/payment-integrations/{paymentIntegrationId}.get.responses.200.description").`should not be empty`()
        }
    }
}
