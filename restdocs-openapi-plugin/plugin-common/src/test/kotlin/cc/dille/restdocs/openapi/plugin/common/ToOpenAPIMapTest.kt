package cc.dille.restdocs.openapi.plugin.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.jayway.jsonpath.JsonPath
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be empty`
import org.junit.Test


class ToOpenAPIMapTest : FragmentFixtures {

    val objectMapper = ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)

    @Test
    fun `should convert minimal resource to openAPI map`() {
        val fragments = listOf(
                OpenAPIFragment("cart-line-item-update", "/carts/{id}",
                        Method(method = "put", summary = "put-update", responses = listOf(Response(200, "description")))
                ),
                OpenAPIFragment("cart-get", "/carts/{id}",
                        Method(method = "get", summary = "get-cart", responses = listOf(Response(201, "description")))
                )
        )

        val openAPIMap = OpenAPIResource.fromFragments(fragments, NoOpJsonSchemaMerger).toOpenAPIMap()

        with(JsonPath.parse(objectMapper.writeValueAsString(openAPIMap))) {
            read<String>("/carts/{id}.put.responses.200.description").`should not be empty`()
            read<String>("/carts/{id}.get.responses.201.description").`should not be empty`()
        }
    }

    @Test
    fun `should convert full resource to openAPI map`() {
        val fragments = listOf(OpenAPIFragment.fromYamlMap("some", parsedFragmentMap { rawFullFragment() }))

        val openAPIMap = OpenAPIResource.fromFragments(fragments, NoOpJsonSchemaMerger).toOpenAPIMap()

        with(JsonPath.parse(objectMapper.writeValueAsString(openAPIMap))) {
            read<List<Map<*, *>>>("/tags/{id}.put.parameters").size `should be equal to` 4
            read<List<Map<*, *>>>("/tags/{id}.put.parameters").forEach {
                (it["name"] as String).`should not be empty`()
                (it["in"] as String).`should not be empty`()
                (it["description"] as String).`should not be empty`()
                ((it["schema"] as Map<*, *>)["type"] as String).`should not be empty`()
                (it["example"] as? String)?.`should not be empty`()
            }

            read<String>("/tags/{id}.put.requestBody.content.application/hal+json.schema.location").`should not be empty`()
            read<String>("/tags/{id}.put.requestBody.content.application/hal+json.example.location").`should not be empty`()
            read<String>("/tags/{id}.put.responses.200.headers.X-Custom-ResponseHeader.description").`should not be empty`()
            read<String>("/tags/{id}.put.responses.200.headers.X-Custom-ResponseHeader.example").`should not be empty`()
            read<String>("/tags/{id}.put.responses.200.content.application/hal+json.schema.location").`should not be empty`()
            read<String>("/tags/{id}.put.responses.200.content.application/hal+json.example.location").`should not be empty`()
        }
    }

    @Test
    fun `should convert resource with empty response`() {
        val fragments = listOf(OpenAPIFragment.fromYamlMap("some", parsedFragmentMap { rawFragmentWithEmptyResponse() }))

        val openAPIMap = OpenAPIResource.fromFragments(fragments, NoOpJsonSchemaMerger).toOpenAPIMap()

        with(JsonPath.parse(objectMapper.writeValueAsString(openAPIMap))) {
            with(read<List<Map<*, *>>>("/payment-integrations/{paymentIntegrationId}.get.parameters").first()) {
                (this["name"] as String).`should not be empty`()
                (this["in"] as String).`should not be empty`()
                (this["description"] as String).`should not be empty`()
                ((this["schema"] as Map<*, *>)["type"] as String).`should not be empty`()
                (this["example"] as String).`should not be empty`()
            }
            read<Map<String, *>>("/payment-integrations/{paymentIntegrationId}.get.responses.200").`should not be empty`()
            read<String>("/payment-integrations/{paymentIntegrationId}.get.responses.200.description").`should not be empty`()
        }
    }
}
