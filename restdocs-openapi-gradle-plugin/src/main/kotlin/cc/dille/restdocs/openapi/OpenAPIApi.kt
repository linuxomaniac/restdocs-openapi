package cc.dille.restdocs.openapi

import cc.dille.restdocs.openapi.OpenAPIVersion.V_3_0_1
import java.io.File

data class OpenAPIApi(val title: String, val baseUri: String?, val openAPIVersion: OpenAPIVersion, private val _resourceGroups: List<ResourceGroup> ) {
    val resourceGroups by lazy {
        _resourceGroups.sortedBy { it.firstPathPart }
    }

    fun toMainFileMap(groupFileNameProvider: (String) -> String) =
            mapOf("title" to title)
                    .let { if (baseUri != null) it.plus("baseUri" to baseUri) else it }
                    .plus(resourceGroups.map { it.firstPathPart to Include(groupFileNameProvider(it.firstPathPart)) } )
                    .toMap()

//    fun toResourceGroupRamlMaps(openAPIVersion: OpenAPIVersion) = resourceGroups.map { it.toOpenAPIMap(openAPIVersion) }
}

enum class OpenAPIVersion(val versionString: String) {
    V_3_0_1("3.0.1")
}

interface ToOpenAPIMap { fun toOpenAPIMap(openAPIVersion: OpenAPIVersion): Map<*, *> }

data class ResourceGroup(val firstPathPart: String, private val _openAPIS: List<OpenAPI>): ToOpenAPIMap {
    val openAPIResources by lazy {
        _openAPIS.map { it.copy(path = it.path.removePrefix(firstPathPart)) }.sortedBy { it.path.length }
    }

    override fun toOpenAPIMap(openAPIVersion: OpenAPIVersion): Map<*, *> =
        openAPIResources.flatMap { it.toOpenAPIMap(openAPIVersion).toList() }.toMap()
}

data class Parameter(val name: String, val in_: String, val description: String, val required: String, val type: String, val example: String): ToOpenAPIMap {
    override fun toOpenAPIMap(openAPIVersion: OpenAPIVersion): Map<*, *> =
        mapOf(name to mapOf(
                "in" to in_,
                "description" to description,
                "required" to required,
                "type" to type,
                "example" to example
        ))
}

fun List<ToOpenAPIMap>.toOpenAPIMap(key: String, openAPIVersion: OpenAPIVersion): Map<*, *> =
        toOpenAPIMap(openAPIVersion)
                .let { if (it.isEmpty()) it else mapOf(key to it) }

fun List<ToOpenAPIMap>.toOpenAPIMap(openAPIVersion: OpenAPIVersion): Map<*, *> =
        this.flatMap { it.toOpenAPIMap(openAPIVersion).toList() }.toMap()

data class Content(val contentType: String,
                val example: Include? = null,
                val schema: Include? = null,
                val examples: List<Include> = emptyList()): ToOpenAPIMap {

    override fun toOpenAPIMap(openAPIVersion: OpenAPIVersion): Map<*, *> {

        return mapOf(contentType to
                when (openAPIVersion) {
                    V_3_0_1 -> mapOf("examples" to examples.map { it.location
                            .replace("-request.json", "")
                            .replace("-response.json", "") to it }.toMap())
                }.let {
                    if (schema != null) it.plus("type" to schema)
                    else it
                }
        )
    }
}

data class Response(val status: Int,
                    val description: String? = null,
                    val contents: List<Content> = emptyList(),
                    val headers: List<Header> = emptyList()): ToOpenAPIMap {
    override fun toOpenAPIMap(openAPIVersion: OpenAPIVersion): Map<*, *> =
            if (contents.isEmpty() && headers.isEmpty())
                mapOf(status to null)
            else
                mapOf(status to (if (contents.isEmpty()) emptyMap<String, Any>() else contents.toOpenAPIMap("content", openAPIVersion))
                        .plus(headers.toOpenAPIMap("headers", openAPIVersion))
                )
}

data class Method(val method: String,
                  val parameters: List<Parameter> = emptyList(),
                  val requestsContents: List<Content> = emptyList(),
                  val responses: List<Response> = emptyList()): ToOpenAPIMap {

    override fun toOpenAPIMap(openAPIVersion: OpenAPIVersion): Map<*, *> =
            mapOf(method to parameters.toOpenAPIMap("parameters", openAPIVersion))
                    .plus(requestsContents.toOpenAPIMap("content", openAPIVersion))
                    .plus(responses.toOpenAPIMap("responses", openAPIVersion))
}

data class Header(val name: String, val description: String, val example: String): ToOpenAPIMap {
    override fun toOpenAPIMap(openAPIVersion: OpenAPIVersion): Map<*, *> =
            mapOf(name to mapOf(
                    "description" to description,
                    "example" to example
            ))
}

data class OpenAPI(val path: String,
                   val methods: List<Method> = emptyList(),
                   val uriParameters: List<Parameter> = emptyList()): ToOpenAPIMap {
    val firstPathPart by lazy {
        path.split("/").find { !it.isEmpty() }?.let{ "/$it" }?:"/"
    }

    override fun toOpenAPIMap(openAPIVersion: OpenAPIVersion): Map<*, *> =
            uriParameters.toOpenAPIMap("parameters", openAPIVersion)
                    .plus(methods.flatMap { it.toOpenAPIMap(openAPIVersion).toList() }.toMap() )
                    .let { if (path.isEmpty()) it else mapOf(path to it)}

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
                        examples = contents.mapNotNull { it.example },
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
            val parameters = (values["parameters"] as? Map<*,*>).orEmpty()
            val methodMap = values.filterKeys { it != "parameters" }
            return OpenAPIFragment(
                    id = id,
                    path = path as String,
                    method = method(methodMap)
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

        private fun content(map: Map<*,*>): Content {
            val contentType = map.keys.first() as String
            val values = map[contentType] as Map<*,*>
            return Content(
                    contentType = contentType,
                    example = values["example"] as Include,
                    schema = values["schema"] as? Include
            )
        }

        private fun response(map: Map<*,*>): Response {
            val status = map.keys.first() as Int
            val values = (map[status] as? Map<*,*>).orEmpty()
            return Response(
                    status = status,
                    description = map["description"] as? String,
                    headers = headers((values["headers"] as? Map<*, *>).orEmpty()),
                    contents = if (values["content"] == null) emptyList() else listOf(content(values["content"] as Map<*, *>))
            )
        }

        private fun method(map: Map<*,*>): Method {
            val methodContent = map[map.keys.first()] as Map<*,*>
            val response = methodContent["responses"] as? Map<*,*>
            return Method(
                    method = map.keys.first() as String,
                    requestsContents = (methodContent["content"] as? Map<*, *>)?.let { listOf(content(it)) }.orEmpty(),
                    parameters = parameters((methodContent["parameters"] as? Map<*, *>).orEmpty()),
                    responses = response?.let { listOf(response(it)) }.orEmpty()
            )
        }

        private fun parameters(map: Map<*,*>): List<Parameter> {
            return map.map { (key, value) -> with(value as Map<*, *>) {
                Parameter(key as String, value["in"] as String, value["description"] as String, value["required"] as String, value["type"] as String, value["example"] as String)
            } }
        }

        private fun headers(map: Map<*,*>): List<Header> {
            return map.map { (key, value) -> with(value as Map<*, *>) {
                Header(key as String, value["description"] as String, value["example"] as String)
            } }
        }
    }
}
