package cc.dille.restdocs.openapi

import java.io.File

data class OpenAPIApi(val title: String, val baseUri: String?, private val _resourceGroups: List<ResourceGroup>) {
    val resourceGroups by lazy {
        _resourceGroups.sortedBy { it.firstPathPart }
    }

    fun toMainFileMap(groupFileNameProvider: (String) -> String) =
            mapOf("title" to title)
                    .let { if (baseUri != null) it.plus("baseUri" to baseUri) else it }
                    .plus(resourceGroups.map { it.firstPathPart to Include(groupFileNameProvider(it.firstPathPart)) })
                    .toMap()

//    fun toResourceGroupRamlMaps(openAPIVersion: OpenAPIVersion) = resourceGroups.map { it.toOpenAPIMap(openAPIVersion) }
}

interface ToOpenAPIMap {
    fun toOpenAPIMap(): Map<*, *>
}

data class ResourceGroup(val firstPathPart: String, private val _openAPIS: List<OpenAPI>) : ToOpenAPIMap {
    val openAPIResources by lazy {
        _openAPIS.map { it.copy(path = it.path.removePrefix(firstPathPart)) }.sortedBy { it.path.length }
    }

    override fun toOpenAPIMap(): Map<*, *> =
            openAPIResources.flatMap { it.toOpenAPIMap().toList() }.toMap()
}

data class Parameter(val name: String, val in_: String, val description: String, val required: String, val type: String, val example: String) : ToOpenAPIMap {
    override fun toOpenAPIMap(): Map<*, *> =
            mapOf(name to mapOf(
                    "in" to in_,
                    "description" to description,
                    "required" to required,
                    "type" to type,
                    "example" to example
            ))
}

fun List<ToOpenAPIMap>.toOpenAPIMap(key: String): Map<*, *> =
        toOpenAPIMap()
                .let { if (it.isEmpty()) it else mapOf(key to it) }

fun List<ToOpenAPIMap>.toOpenAPIMap(): Map<*, *> =
        this.flatMap { it.toOpenAPIMap().toList() }.toMap()

data class Content(val contentType: String,
                   val schema: Include? = null,
                   val examples: List<Include> = emptyList()) : ToOpenAPIMap {

    override fun toOpenAPIMap(): Map<*, *> {
        var nex = 0

        return mapOf(contentType to
                listOfNotNull(
                        if (schema != null) "schema" to mapOf("\$ref" to schema) else null,
                        if (!examples.isEmpty()) "examples" to examples.map { ex -> mapOf("example" + nex++.toString() to mapOf("\$ref" to ex)) } else null
                ).toMap()
        )
    }
}

data class Response(val status: Int,
                    val description: String? = null,
                    val contents: List<Content> = emptyList(),
                    val headers: List<Header> = emptyList()) : ToOpenAPIMap {
    override fun toOpenAPIMap(): Map<*, *> =
            mapOf(status to (if (contents.isEmpty()) emptyMap<String, Any>() else contents.toOpenAPIMap("content"))
                    .plus(headers.toOpenAPIMap("headers"))
            )
}

data class Method(val method: String,
                  val parameters: List<Parameter> = emptyList(),
                  val requestsContents: List<Content> = emptyList(),
                  val responses: List<Response> = emptyList()) : ToOpenAPIMap {

    override fun toOpenAPIMap(): Map<*, *> =
            mapOf(method to parameters.toOpenAPIMap("parameters"))
                    .plus(requestsContents.toOpenAPIMap("content"))
                    .plus(responses.toOpenAPIMap("responses"))
}

data class Header(val name: String, val description: String, val example: String) : ToOpenAPIMap {
    override fun toOpenAPIMap(): Map<*, *> =
            mapOf(name to mapOf(
                    "description" to description,
                    "example" to example
            ))
}

data class OpenAPI(val path: String,
                   val methods: List<Method> = emptyList(),
                   val uriParameters: List<Parameter> = emptyList()) : ToOpenAPIMap {
    val firstPathPart by lazy {
        path.split("/").find { !it.isEmpty() }?.let { "/$it" } ?: "/"
    }

    override fun toOpenAPIMap(): Map<*, *> =
            uriParameters.toOpenAPIMap("parameters")
                    .plus(methods.flatMap { it.toOpenAPIMap().toList() }.toMap())
                    .let { if (path.isEmpty()) it else mapOf(path to it) }

    companion object {
        fun fromFragments(allFragments: List<OpenAPIFragment>, jsonSchemaMerger: JsonSchemaMerger): OpenAPI {
            if (allFragments.groupBy { it.path }.size > 1)
                throw IllegalArgumentException("Fragments for a resource must have a common path")

            val methods = allFragments
                    .groupBy { it.method.method }
                    .map { (_, fragments) ->
                        val contentsByContentType = fragments
                                .mapNotNull { it.method.requestsContents.firstOrNull() }
                                .groupBy { it.contentType }
                        val responsesByStatus = fragments
                                .mapNotNull { it.method.responses.firstOrNull() }
                                .groupBy { it.status }

                        fragments.first().method.copy(
                                requestsContents = mergeBodiesWithSameContentType(contentsByContentType, jsonSchemaMerger),
                                responses = mergeResponsesWithSameStatusAndContentType(responsesByStatus, jsonSchemaMerger)
                        )
                    }

            return OpenAPI(allFragments.first().path, methods)
        }

        private fun mergeBodiesWithSameContentType(
                contentsByContentType: Map<String, List<Content>>,
                jsonSchemaMerger: JsonSchemaMerger): List<Content> {
            return contentsByContentType.map { (contentType, contents) ->
                Content(
                        contentType = contentType,
                        examples = contents.mapNotNull { it.examples }.flatten(),
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
                        headers = responses.flatMap { it.headers },
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
        @Suppress("UNCHECKED_CAST")
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

            val schema = (values["schema"] as? Map<*, *>).orEmpty()

            System.out.println(schema)

            var examples = (values["examples"] as? Map<*, *>).orEmpty()
            if (!examples.isEmpty()) {
                examples = examples[examples.keys.first()] as Map<*, *>
            }

            return Content(
                    contentType = contentType,
                    example = if (!examples.isEmpty()) Include(examples["\$ref"] as String) else null,
                    schema = if (!schema.isEmpty()) Include(schema["\$ref"] as String) else null
            )
        }

        private fun response(map: Map<*, *>): Response {
            val status = map.keys.first() as Int
            val values = (map[status] as? Map<*, *>).orEmpty()
            return Response(
                    status = status,
                    description = values["description"] as? String,
                    headers = headers((values["headers"] as? Map<*, *>).orEmpty()),
                    contents = if (values["content"] == null) emptyList() else listOf(content(values["content"] as Map<*, *>))
            )
        }

        private fun method(map: Map<*, *>): Method {
            val methodContent = map[map.keys.first()] as Map<*, *>
            val response = methodContent["responses"] as? Map<*, *>
            return Method(
                    method = map.keys.first() as String,
                    requestsContents = (methodContent["content"] as? Map<*, *>)?.let { listOf(content(it)) }.orEmpty(),
                    parameters = parameters((methodContent["parameters"] as? List<*>).orEmpty()),
                    responses = response?.let { listOf(response(it)) }.orEmpty()
            )
        }

        private fun parameters(list: List<*>): List<Parameter> {
            return list.map { value ->
                with(value as Map<*, *>) {
                    Parameter(
                            value["name"] as String,
                            value["in"] as String,
                            value["description"] as String,
                            value["required"].toString(),
                            (value["schema"] as Map<*, *>)["type"] as String,
                            value["example"].toString()
                    )
                }
            }
        }

        private fun headers(map: Map<*, *>): List<Header> {
            return map.map { (key, value) ->
                with(value as Map<*, *>) {
                    Header(key as String, value["description"] as String, value["example"] as String)
                }
            }
        }
    }
}
