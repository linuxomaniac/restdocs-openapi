package cc.dille.restdocs.openapi.plugin.common

import java.io.File

data class OpenAPIApi(var openAPIVersion: String,
                      var infoVersion: String,
                      var infoTitle: String,
                      var infoDescription: String? = null,
                      var infoContactName: String? = null,
                      var infoContactEmail: String? = null,
                      var infoContactUrl: String? = null,
                      var serverUrl: String? = null,
                      var serverDescription: String? = null,
                      private val resourceGroups: List<ResourceGroup>) {

    fun toMainFileMap() =
            listOfNotNull(
                    "openapi" to openAPIVersion,
                    "info" to listOfNotNull(
                            "version" to infoVersion,
                            "title" to infoTitle,
                            if (infoContactEmail != null || infoContactName != null) "contact" to listOfNotNull(
                                    infoContactEmail?.let { "email" to it },
                                    infoContactName?.let { "name" to it },
                                    infoContactUrl?.let { "url" to it }
                            ).toMap()
                            else null,
                            infoDescription?.let { "description" to it }
                    ).toMap(),
                    if (serverUrl != null || serverDescription != null)
                        "servers" to listOf(listOfNotNull(
                                serverDescription?.let { "description" to it },
                                serverUrl?.let { "url" to it }
                        ).toMap())
                    else null,
                    "paths" to resourceGroups.toOpenAPIMap()
            ).toMap()
}

interface ToOpenAPIMap {
    fun toOpenAPIMap(): Map<*, *>
}

data class ResourceGroup(val path: String, private val openAPIResources: List<OpenAPIResource>) : ToOpenAPIMap {
    override fun toOpenAPIMap(): Map<*, *> =
            openAPIResources.flatMap { it.toOpenAPIMap().toList() }.toMap()
}

data class Parameter(val name: String, val in_: String, val description: String?, val required: Boolean?, val type: String?, val example: String?) : ToOpenAPIMap {
    override fun toOpenAPIMap(): Map<*, *> =
            listOfNotNull("name" to name,
                    "in" to in_,
                    description?.let { "description" to it },
                    required?.let { "required" to it },
                    type?.let { "schema" to mapOf("type" to it) },
                    example?.let { "example" to it }
            ).toMap()
}

fun List<ToOpenAPIMap>.toOpenAPIMap(key: String): Map<*, *> =
        toOpenAPIMap()
                .let { if (it.isEmpty()) it else mapOf(key to it) }

fun List<ToOpenAPIMap>.toOpenAPIMap(): Map<*, *> =
        this.flatMap { it.toOpenAPIMap().toList() }.toMap()

data class RequestContent(val required: Boolean = false,
                          val contents: List<Content> = emptyList()) : ToOpenAPIMap {

    override fun toOpenAPIMap(): Map<*, *> {
        return if (!contents.isEmpty()) mapOf("requestBody" to mapOf("required" to required, "content" to contents.toOpenAPIMap())) else emptyMap<String, String>()
    }
}

data class Content(val contentType: String,
                   val schema: Include? = null,
                   val example: Include? = null) : ToOpenAPIMap {

    override fun toOpenAPIMap(): Map<*, *> =
            mapOf(contentType to
                    listOfNotNull(
                            schema?.let { "schema" to schema },
                            example?.let { "example" to example }
                    ).toMap()
            )
}

data class Response(val status: Int,
                    val description: String? = null,
                    val contents: List<Content> = emptyList(),
                    val headers: List<ResponseHeader> = emptyList(),
                    val links: List<Link> = emptyList()) : ToOpenAPIMap {
    override fun toOpenAPIMap(): Map<*, *> =
            mapOf(status to mapOf("description" to description)
                    .plus(headers.toOpenAPIMap("headers"))
                    .plus(links.toOpenAPIMap("links"))
                    .plus(contents.toOpenAPIMap("content"))
            )
}

data class Method(val method: String,
                  val summary: String? = null,
                  val operationId: String? = null,
                  val parameters: List<Parameter> = emptyList(),
                  val requestContent: RequestContent? = null,
                  val responses: List<Response> = emptyList()) : ToOpenAPIMap {

    override fun toOpenAPIMap(): Map<*, *> =
            mapOf(method to listOfNotNull(
                    summary?.let{"summary" to summary},
                    operationId?.let { "operationId" to it },
                    if (!parameters.isEmpty()) "parameters" to parameters.map { it.toOpenAPIMap() } else null
            ).toMap()
                    .plus(requestContent?.toOpenAPIMap() ?: emptyMap())
                    .plus(responses.toOpenAPIMap("responses")))
}

data class ResponseHeader(val name: String, val description: String?, val example: String? = null) : ToOpenAPIMap {
    override fun toOpenAPIMap(): Map<*, *> =
            mapOf(name to listOfNotNull(
                    description?.let { "description" to it },
                    example?.let { "example" to it }
            ).toMap())
}

data class LinkParameter(val name: String, val location: String) : ToOpenAPIMap {
    override fun toOpenAPIMap(): Map<*, *> =
            mapOf(name to location)
}

data class Link(val rel: String,
                val operationId: String,
                val description: String? = null,
                val parameters: List<LinkParameter> = emptyList()) : ToOpenAPIMap {

    override fun toOpenAPIMap(): Map<*, *> =
            mapOf(rel to
                    listOfNotNull(
                            "operationId" to operationId,
                            description?.let { "description" to description }
                    ).toMap()
                            .plus(parameters.toOpenAPIMap("parameters"))
            )
}

data class OpenAPIResource(val path: String, val methods: List<Method> = emptyList()) : ToOpenAPIMap {
    override fun toOpenAPIMap(): Map<*, *> =
            methods.flatMap { it.toOpenAPIMap().toList() }.toMap().let { if (path.isEmpty()) it else mapOf(path to it) }

    companion object {
        fun fromFragments(allFragments: List<OpenAPIFragment>, jsonSchemaMerger: JsonSchemaMerger): OpenAPIResource {
            if (allFragments.groupBy { it.path }.size > 1)
                throw IllegalArgumentException("Fragments for a resource must have a common path")

            val methods = allFragments
                    .groupBy { it.method.method }
                    .map { (_, fragments) ->
                        val contentsByContentType = fragments
                                .mapNotNull { it.method.requestContent?.contents?.firstOrNull() }
                                .groupBy { it.contentType }
                        val responsesByStatus = fragments
                                .mapNotNull { it.method.responses.firstOrNull() }
                                .groupBy { it.status }

                        fragments.first().method.copy(
                                requestContent = RequestContent(fragments.any {
                                    it.method.requestContent?.required ?: false
                                }, mergeBodiesWithSameContentType(contentsByContentType, jsonSchemaMerger)),
                                // We take the longer summary!
                                summary = fragments.map { it.method.summary }.sortedBy { it?.length }.last(),
                                // We do the same for OperationId, because we want to exclude the null ones
                                operationId = fragments.map { it.method.operationId }.sortedBy { it?.length }.last(),
                                responses = mergeResponsesWithSameStatusAndContentType(responsesByStatus, jsonSchemaMerger)
                        )
                    }

            return OpenAPIResource(allFragments.first().path, methods)
        }

        private fun mergeBodiesWithSameContentType(
                contentsByContentType: Map<String, List<Content>>,
                jsonSchemaMerger: JsonSchemaMerger): List<Content> {
            return contentsByContentType.map { (contentType, contents) ->
                Content(
                        contentType = contentType,
                        example = contents.map { it.example }.first(),
                        schema = contents.mapNotNull { it.schema }
                                .let { if (it.isNotEmpty()) jsonSchemaMerger.mergeSchemas(it) else null }
                )
            }
        }

        private fun mergeResponsesWithSameStatusAndContentType(
                responsesByStatus: Map<Int, List<Response>>,
                jsonSchemaMerger: JsonSchemaMerger): List<Response> {
            return responsesByStatus.map { (status, responses) ->
                Response(
                        status = status,
                        description = responses.first().description,
                        headers = responses.flatMap { it.headers },
                        links = responses.flatMap { it.links },
                        contents = mergeBodiesWithSameContentType(responses
                                .flatMap { it.contents }
                                .groupBy { it.contentType }, jsonSchemaMerger)
                )
            }
        }
    }
}

data class OpenAPIFragment(val id: String,
                           val path: String,
                           val method: Method) {

    companion object {
        fun fromYamlMap(id: String, yamlMap: Map<*, *>): OpenAPIFragment {

            val path = yamlMap.keys.first()
            val values = yamlMap[path] as Map<*, *>
            return OpenAPIFragment(
                    id = id,
                    path = path as String,
                    method = method(values)
            )
        }

        fun fromFile(file: File): OpenAPIFragment {
            val id = file.path
                    .removeSuffix(file.name)
                    .removeSuffix(File.separator)
                    .split(File.separator)
                    .let { it[it.size - 1] }
            return fromYamlMap(id, OpenAPIParser.parseFragment(file))
        }

        private fun content(map: Map<*, *>): Content {
            val contentType = map.keys.first() as String
            val values = map[contentType] as Map<*, *>

            return Content(
                    contentType = contentType,
                    example = values["example"] as? Include,
                    schema = values["schema"] as? Include
            )
        }

        private fun requestBody(map: Map<*, *>): RequestContent? {
            if (map.isEmpty()) {
                return null
            }

            return RequestContent(
                    required = map["required"] as? Boolean ?: false,
                    contents = (map["content"] as? Map<*, *>)?.let { listOf(content(it)) }.orEmpty()
            )
        }

        private fun response(map: Map<*, *>): Response {
            val status = map.keys.first() as Int
            val values = (map[status] as? Map<*, *>).orEmpty()
            return Response(
                    status = status,
                    description = values["description"] as? String,
                    headers = responseHeaders((values["headers"] as? Map<*, *>).orEmpty()),
                    links = links((values["links"] as? Map<*, *>).orEmpty()),
                    contents = (values["content"] as? Map<*, *>)?.let { listOf(content(it)) }.orEmpty()
            )
        }

        private fun method(map: Map<*, *>): Method {
            val methodContent = map[map.keys.first()] as Map<*, *>
            return Method(
                    method = map.keys.first() as String,
                    summary = methodContent["summary"] as? String,
                    operationId = methodContent["operationId"] as String?,
                    requestContent = requestBody((methodContent["requestBody"] as? Map<*, *>).orEmpty()),
                    parameters = parameters((methodContent["parameters"] as? List<*>).orEmpty()),
                    responses = (methodContent["responses"] as? Map<*, *>)?.let { listOf(response(it)) }.orEmpty()
            )
        }

        private fun parameters(list: List<*>): List<Parameter> =
                list.map { value ->
                    with(value as Map<*, *>) {
                        Parameter(
                                value["name"] as String,
                                value["in"] as String,
                                value["description"] as String?,
                                value["required"] as Boolean?,
                                (value["schema"] as? Map<*, *>)?.let { it["type"] as? String },
                                value["example"]?.toString()
                        )
                    }
                }

        private fun responseHeaders(map: Map<*, *>): List<ResponseHeader> =
                map.map { (key, value) ->
                    with(value as Map<*, *>) {
                        ResponseHeader(key as String, value["description"] as? String, value["example"] as? String)
                    }
                }

        private fun linkParameters(map: Map<*, *>?): List<LinkParameter> =
                map?.map { (k, v) -> LinkParameter(k as String, v as String) }?.toList().orEmpty()

        private fun links(map: Map<*, *>): List<Link> {
            return map.map { (key, value) ->
                with(value as Map<*, *>) {
                    Link(key as String, value["operationId"] as String, value["description"] as? String, linkParameters(value["parameters"] as? Map<*, *>))
                }
            }
        }
    }
}
