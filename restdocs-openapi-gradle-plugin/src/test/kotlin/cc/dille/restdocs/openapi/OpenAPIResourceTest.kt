package cc.dille.restdocs.openapi

import org.amshove.kluent.`should be empty`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should throw`
import org.junit.Test


class OpenAPIResourceTest {

    @Test
    fun `should merge fragments with the same method`() {
        val fragments = listOf(
                OpenAPIFragment("cart-get", "/carts/{id}",
                        Method(
                                method = "get",
                                requestsContents = listOf(
                                        Content("application/json", Include("cart-get-request-schema.json"), listOf(Include("cart-get-request.json")))),
                                responses = listOf(Response(
                                        status = 200,
                                        description = "description",
                                        contents = listOf(Content("application/json", Include("cart-get-response-schema.json"), listOf(Include("cart-get-response.json"))))
                                ))
                        )
                ),
                OpenAPIFragment("cart-get-additional", "/carts/{id}",
                        Method(
                                method = "get",
                                requestsContents = listOf(
                                        Content("application/json", Include("cart-get-additional-request-schema.json"), listOf(Include("cart-get-additional-request.json")))),
                                responses = listOf(Response(
                                        status = 200,
                                        description = "description",
                                        contents = listOf(Content("application/json", Include("cart-get-additional-response-schema.json"), listOf(Include("cart-get-additional-response.json"))))
                                ))
                        )
                )
        )
        val resource = OpenAPIResource.fromFragments(fragments, NoOpJsonSchemaMerger)

        with(resource) {
            path `should equal` "/carts/{id}"
            methods.size `should equal` 1
            methods.first().requestsContents.size `should equal` 1
            with(methods.first().requestsContents.first()) {
                contentType `should equal` "application/json"
                examples `should equal` listOf(Include("cart-get-request.json"), Include("cart-get-additional-request.json"))
                schema `should equal` Include("cart-get-request-schema.json")
            }

            methods.first().responses.size `should equal` 1
            with(methods.first().responses.first()) {
                contents.size `should be` 1
                contents.first().contentType `should equal` "application/json"
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
                                requestsContents = listOf(
                                        Content("application/json", Include("cart-line-item-update-request.json"), listOf(Include("cart-line-item-update-schema.json")))),
                                responses = listOf(Response(
                                        status = 200,
                                        description = "description",
                                        contents = listOf(Content("application/json", Include("cart-line-item-update-response.json"), listOf(Include("cart-line-item-update-response-schema.json"))))
                                ))
                        )
                ),
                OpenAPIFragment("cart-line-item-assign", "/carts/{id}/line-items",
                        Method(
                                method = "put",
                                requestsContents = listOf(
                                        Content("text/uri-list", Include("cart-line-item-assign-request.json"), listOf(Include("cart-line-item-assign-schema.json")))),
                                responses = listOf(Response(
                                        status = 200,
                                        description = "description",
                                        contents = listOf(Content("text/uri-list", Include("cart-line-item-assign-response.json"), listOf(Include("cart-line-item-assign-response-schema.json"))))
                                ))
                        )
                )
        )

        val resource = OpenAPIResource.fromFragments(fragments, NoOpJsonSchemaMerger)

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
                        Method(method = "put")
                ),
                OpenAPIFragment("cart-get", "/carts/{id}",
                        Method(method = "get")
                )
        )

        val fromFragmentsFunctions = { OpenAPIResource.fromFragments(fragments, NoOpJsonSchemaMerger) }
        fromFragmentsFunctions `should throw` IllegalArgumentException::class
    }
}
