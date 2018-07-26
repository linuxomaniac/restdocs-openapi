package cc.dille.restdocs.openapi

import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should throw`
import org.junit.Test


class OpenAPITest {

    @Test
    fun `should merge fragments with the same method`() {
        val fragments = listOf(
                OpenAPIFragment("cart-get", "/carts/{id}",
                        Method(
                                method = "get",
                                description = "description",
                                requestsContents = listOf(
                                        Content("application/json", Include("cart-get-request.json"), Include("cart-get-request-schema.json"))),
                                responses = listOf(Response(200,
                                        listOf(Content("application/json", Include("cart-get-response.json"), Include("cart-get-response-schema.json")))))
                        )
                ),
                OpenAPIFragment("cart-get-additional", "/carts/{id}",
                        Method(
                                method = "get",
                                description = "description",
                                requestsContents = listOf(
                                        Content("application/json", Include("cart-get-additional-request.json"), Include("cart-get-additional-schema.json"))),
                                responses = listOf(Response(200,
                                        listOf(Content("application/json", Include("cart-get-additional-response.json"), Include("cart-get-additional-response-schema.json")))))
                        )
                )
        )
        val resource = OpenAPI.fromFragments(fragments, NoOpJsonSchemaMerger)

        with(resource) {
            path `should equal` "/carts/{id}"
            methods.size `should equal` 1
            methods.first().requestsContents.size `should equal` 1
            with(methods.first().requestsContents.first()) {
                contentType `should equal` "application/json"
                example.`should be null`()
                examples `should equal` listOf(Include("cart-get-request.json"), Include("cart-get-additional-request.json"))
                schema `should equal` Include("cart-get-request-schema.json")
            }

            methods.first().responses.size `should equal` 1
            with(methods.first().responses.first()) {
                contents.size `should be` 1
                contents.first().contentType `should equal` "application/json"
                contents.first().example.`should be null`()
                contents.first().examples `should equal` listOf(Include("cart-get-response.json"), Include("cart-get-additional-response.json"))
                contents.first().schema `should equal` Include("cart-get-response-schema.json")
            }
        }
    }

    @Test
    fun `should add requests with different content types`() {
        val fragments = listOf(
                OpenAPIFragment("cart-line-item-update", "/carts/{id}/line-items",
                        Method(
                                method = "put",
                                description = "description",
                                requestsContents = listOf(
                                        Content("application/json", Include("cart-line-item-update-request.json"), Include("cart-line-item-update-schema.json"))),
                                responses = listOf(Response(200,
                                        listOf(Content("application/json", Include("cart-line-item-update-response.json"), Include("cart-line-item-update-response-schema.json")))))
                        )
                ),
                OpenAPIFragment("cart-line-item-assign", "/carts/{id}/line-items",
                        Method(
                                method = "put",
                                description = "description",
                                requestsContents = listOf(
                                        Content("text/uri-list", Include("cart-line-item-assign-request.json"), Include("cart-line-item-assign-schema.json"))),
                                responses = listOf(Response(200,
                                        listOf(Content("text/uri-list", Include("cart-line-item-assign-response.json"), Include("cart-line-item-assign-response-schema.json")))))
                        )
                )
        )

        val resource = OpenAPI.fromFragments(fragments, NoOpJsonSchemaMerger)

        with(resource) {
            path `should equal` "/carts/{id}/line-items"
            methods.size `should equal` 1
            with(methods.first()) {
                requestsContents.size `should equal` 2
                requestsContents.map { it.contentType } `should equal` listOf("application/json", "text/uri-list")
                responses.size `should equal` 1
                responses.first().contents.size `should equal` 2
                responses.first().contents.map { it.contentType } `should equal` listOf("application/json", "text/uri-list")
            }
        }
    }

    @Test
    fun `should fail on fragments with different path`() {
        val fragments = listOf(
                OpenAPIFragment("cart-line-item-update", "/carts/{id}/line-items",
                        Method(method = "put", description = "description")
                ),
                OpenAPIFragment("cart-get", "/carts/{id}",
                        Method(method = "get", description = "description")
                )
        )

        val fromFragmentsFunctions = { OpenAPI.fromFragments(fragments, NoOpJsonSchemaMerger) }
        fromFragmentsFunctions `should throw` IllegalArgumentException::class
    }
}
