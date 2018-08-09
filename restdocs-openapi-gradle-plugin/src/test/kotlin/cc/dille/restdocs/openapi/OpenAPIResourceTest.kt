package cc.dille.restdocs.openapi

import org.amshove.kluent.*
import org.junit.Test


class OpenAPIResourceTest {

    @Test
    fun `should merge fragments with the same method`() {
        val fragments = listOf(
                OpenAPIFragment("cart-get", "/carts/{id}",
                        Method(
                                method = "get",
                                summary = "get-carts",
                                requestContent = RequestContent(
                                        false,
                                        listOf(Content("application/json", Include("cart-get-request-schema.json"), Include("cart-get-request.json")))
                                ),
                                responses = listOf(Response(
                                        status = 200,
                                        description = "description",
                                        contents = listOf(Content("application/json", Include("cart-get-response-schema.json"), Include("cart-get-response.json")))
                                ))
                        )
                ),
                OpenAPIFragment("cart-get-additional", "/carts/{id}",
                        Method(
                                method = "get",
                                operationId = "getCarts",
                                summary = "get-carts",
                                requestContent = RequestContent(true,
                                        listOf(Content("application/json", Include("cart-get-additional-request-schema.json"), Include("cart-get-additional-request.json")))
                                ),
                                responses = listOf(Response(
                                        status = 200,
                                        description = "description",
                                        contents = listOf(Content("application/json", Include("cart-get-additional-response-schema.json"), Include("cart-get-additional-response.json"))),
                                        links = listOf(Link("self", "getSelf", "sample link"))
                                ))
                        )
                )
        )
        val resource = OpenAPIResource.fromFragments(fragments, NoOpJsonSchemaMerger)

        with(resource) {
            path `should equal` "/carts/{id}"
            methods.size `should equal` 1
            methods.first().requestContent?.required `should be` true
            methods.first().requestContent?.contents?.size `should equal` 1
            methods.first().requestContent?.contents?.`should not be null`()
            with(methods.first().requestContent?.contents!!.first()) {
                contentType `should equal` "application/json"
                example `should equal` Include("cart-get-request.json")
                schema `should equal` Include("cart-get-request-schema.json")
            }

            methods.first().responses.size `should equal` 1
            with(methods.first().responses.first()) {
                contents.size `should be` 1
                contents.first().contentType `should equal` "application/json"
                contents.first().example `should equal` Include("cart-get-response.json")
                contents.first().schema `should equal` Include("cart-get-response-schema.json")
                links.size `should equal` 1
                links.first().rel `should equal` "self"
                links.first().operationId `should equal` "getSelf"
                links.first().description `should equal` "sample link"
            }
        }
    }

    @Test
    fun `should add requests with different content types`() {
        val fragments = listOf(
                OpenAPIFragment("cart-line-item-update", "/carts/{id}/line-items",
                        Method(
                                method = "put",
                                summary = "put-update",
                                requestContent = RequestContent(true,
                                        listOf(Content("application/json", Include("cart-line-item-update-request.json"), Include("cart-line-item-update-schema.json")))
                                ),
                                responses = listOf(Response(
                                        status = 200,
                                        description = "description",
                                        contents = listOf(Content("application/json", Include("cart-line-item-update-response.json"), Include("cart-line-item-update-response-schema.json")))
                                ))
                        )
                ),
                OpenAPIFragment("cart-line-item-assign", "/carts/{id}/line-items",
                        Method(
                                method = "put",
                                summary = "put-assign",
                                requestContent = RequestContent(false,
                                        listOf(Content("text/uri-list", Include("cart-line-item-assign-request.json"), Include("cart-line-item-assign-schema.json")))
                                ),
                                responses = listOf(Response(
                                        status = 200,
                                        description = "description",
                                        contents = listOf(Content("text/uri-list", Include("cart-line-item-assign-response.json"), Include("cart-line-item-assign-response-schema.json")))
                                ))
                        )
                )
        )

        val resource = OpenAPIResource.fromFragments(fragments, NoOpJsonSchemaMerger)

        with(resource) {
            path `should equal` "/carts/{id}/line-items"
            methods.size `should equal` 1
            with(methods.first()) {
                requestContent?.required `should be` true
                requestContent?.contents.`should not be null`()
                requestContent?.contents?.size `should equal` 2
                requestContent?.contents!!.map { it.contentType } `should equal` listOf("application/json", "text/uri-list")
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
                        Method(method = "put", summary = "1")
                ),
                OpenAPIFragment("cart-get", "/carts/{id}",
                        Method(method = "get", summary = "2")
                )
        )

        val fromFragmentsFunctions = { OpenAPIResource.fromFragments(fragments, NoOpJsonSchemaMerger) }
        fromFragmentsFunctions `should throw` IllegalArgumentException::class
    }
}
