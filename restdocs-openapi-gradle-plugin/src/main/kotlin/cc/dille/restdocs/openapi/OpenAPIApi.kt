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
}

interface ToOpenAPIMap {
    fun toOpenAPIMap(): Map<*, *>
}

data class ResourceGroup(val firstPathPart: String, private val _openAPIResources: List<OpenAPIResource>) : ToOpenAPIMap {
    private val openAPIResources by lazy {
        _openAPIResources.map { it.copy(path = it.path.removePrefix(firstPathPart)) }.sortedBy { it.path.length }
    }

    override fun toOpenAPIMap(): Map<*, *> =
            openAPIResources.flatMap { it.toOpenAPIMap().toList() }.toMap()
}

data class Parameter(val name: String, val in_: String, val description: String, val required: String, val type: String, val example: String) : ToOpenAPIMap {
    override fun toOpenAPIMap(): Map<*, *> =
            mapOf("name" to name,
                    "in" to in_,
                    "description" to description,
                    "required" to required,
                    "schema" to mapOf("type" to type),
                    "example" to example
            )
}

fun List<ToOpenAPIMap>.toOpenAPIMap(key: String): Map<*, *> =
        toOpenAPIMap()
                .let { if (it.isEmpty()) it else mapOf(key to it) }

fun List<ToOpenAPIMap>.toOpenAPIMap(): Map<*, *> =
        this.flatMap { it.toOpenAPIMap().toList() }.toMap()

data class RequestBody(val required: Boolean = false,
                       val contents: List<Content> = emptyList()) : ToOpenAPIMap {

    override fun toOpenAPIMap(): Map<*, *> {
        // TODO
        return if (content != null) mapOf("required" to required.toString(), "content" to content.toOpenAPIMap()) else emptyMap<String, String>()
    }
}

data class Content(val contentType: String,
                   val schema: Include? = null,
                   val examples: List<Include> = emptyList()) : ToOpenAPIMap {

    override fun toOpenAPIMap(): Map<*, *> {
        var nex = 0

        return mapOf(contentType to
                listOfNotNull(
                        if (schema != null) "schema" to schema.toMap() else null,
                        if (!examples.isEmpty()) "examples" to examples.flatMap { mapOf("example" + nex++.toString() to it.toMap()).toList() }.toMap() else null
                ).toMap()
        )
    }
}

data class Response(val status: Int,
                    val description: String? = null,
                    val contents: List<Content> = emptyList(),
                    val headers: List<Header> = emptyList()) : ToOpenAPIMap {
    override fun toOpenAPIMap(): Map<*, *> =
            mapOf(status to mapOf("description" to description)
                    .plus(headers.toOpenAPIMap("headers"))
                    .plus(contents.toOpenAPIMap("content"))
            )
}

data class Method(val method: String,
                  val parameters: List<Parameter> = emptyList(),
                  val requestsContents: List<Content> = emptyList(),
                  val responses: List<Response> = emptyList()) : ToOpenAPIMap {

    override fun toOpenAPIMap(): Map<*, *> {
        return mapOf(method to mapOf("parameters" to parameters.map { it.toOpenAPIMap() })
                // TODO : here
                .plus(if (!requestsContents.isEmpty()) mapOf("requestBody" to mapOf("required" to "true", "content" to requestsContents.toOpenAPIMap())) else emptyMap())
                .plus(responses.toOpenAPIMap("responses")))
    }
}

data class Header(val name: String, val description: String, val example: String) : ToOpenAPIMap {
    override fun toOpenAPIMap(): Map<*, *> =
            mapOf(name to mapOf(
                    "description" to description,
                    "example" to example
            ))
}

data class OpenAPIResource(val path: String,
                           val methods: List<Method> = emptyList()) : ToOpenAPIMap {
    val firstPathPart by lazy {
        path.split("/").find { !it.isEmpty() }?.let { "/$it" } ?: "/"
    }

    override fun toOpenAPIMap(): Map<*, *> =
            mapOf(path to methods.flatMap { it.toOpenAPIMap().toList() }.toMap())

    companion object {
        fun fromFragments(allFragments: List<OpenAPIFragment>, jsonSchemaMerger: JsonSchemaMerger): OpenAPIResource {
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

            return OpenAPIResource(allFragments.first().path, methods)
        }

        private fun mergeBodiesWithSameContentType(
                contentsByContentType: Map<String, List<Content>>,
                jsonSchemaMerger: JsonSchemaMerger): List<Content> {
            return contentsByContentType.map { (contentType, contents) ->
                Content(
                        contentType = contentType,
                        examples = contents.map { it.examples }.flatten(),
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

            val examples = (values["examples"] as? Map<*, *>).orEmpty()

            return Content(
                    contentType = contentType,
                    examples = if (!examples.isEmpty()) examples.map { (_, v) -> with(v as Map<*, *>) { Include(v["\$ref"] as String) } } else emptyList(),
                    schema = if (!schema.isEmpty()) Include(schema["\$ref"] as String) else null
            )
        }

        private fun requestBody(map: Map<*, *>): RequestBody? {
            if(map.isEmpty()) {
               return null
            }

            return RequestBody(
                    required = if(map["required"] != null) (map["required"] as String).toBoolean() else false,
                    contents = listOf(content(map["content"] as Map <*, *>))
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
                    requestsContents = requestBody(methodContent["resquestBody"] as Map<*, *>),
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
