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

data class Parameter(val name: String, val description: String, val type: String, val required: String, val in_: String): ToOpenAPIMap {
    override fun toOpenAPIMap(openAPIVersion: OpenAPIVersion): Map<*, *> =
        mapOf(name to mapOf(
                "in" to in_,
                "description" to description,
                "required" to required,
                "type" to type
        ))
}

fun List<ToOpenAPIMap>.toOpenAPIMap(key: String, openAPIVersion: OpenAPIVersion): Map<*, *> =
        toOpenAPIMap(openAPIVersion)
                .let { if (it.isEmpty()) it else mapOf(key to it) }

fun List<ToOpenAPIMap>.toOpenAPIMap(openAPIVersion: OpenAPIVersion): Map<*, *> =
        this.flatMap { it.toOpenAPIMap(openAPIVersion).toList() }.toMap()

data class Body(val contentType: String,
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
                    val bodies: List<Body>,
                    val headers: List<Header> = emptyList()): ToOpenAPIMap {
    override fun toOpenAPIMap(openAPIVersion: OpenAPIVersion): Map<*, *> =
            if (bodies.isEmpty() && headers.isEmpty())
                mapOf(status to null)
            else
                mapOf(status to (if (bodies.isEmpty()) emptyMap<String, Any>() else bodies.toOpenAPIMap("body", openAPIVersion))
                        .plus(headers.toOpenAPIMap("headers", openAPIVersion))
                )
}

data class Method(val method: String,
                  val description: String? = null,
                  val parameters: List<Parameter> = emptyList(),
                  val requestBodies: List<Body> = emptyList(),
                  val responses: List<Response> = emptyList()): ToOpenAPIMap {

    override fun toOpenAPIMap(openAPIVersion: OpenAPIVersion): Map<*, *> =
            mapOf(method to (if (description != null) mapOf("description" to description) else emptyMap())
                    .plus(parameters.toOpenAPIMap("parameters", openAPIVersion))
                    .plus(requestBodies.toOpenAPIMap("body", openAPIVersion))
                    .plus(responses.toOpenAPIMap("responses", openAPIVersion))
            )
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
                        val bodiesByContentType = fragments
                                .mapNotNull { it.method.requestBodies.firstOrNull() }
                                .groupBy { it.contentType }
                        val responsesByStatus = fragments
                                .mapNotNull { it.method.responses.firstOrNull() }
                                .groupBy { it.status }

                        fragments.first().method.copy(
                                requestBodies = mergeBodiesWithSameContentType(bodiesByContentType, jsonSchemaMerger),
                                responses = mergeResponsesWithSameStatusAndContentType(responsesByStatus, jsonSchemaMerger)
                        )
                    }

            return OpenAPI(allFragments.first().path, methods, allFragments.first().parameters)
        }

        private fun mergeBodiesWithSameContentType(
                bodiesByContentType: Map<String, List<Body>>,
                jsonSchemaMerger: JsonSchemaMerger): List<Body> {
            return bodiesByContentType.map { (contentType, bodies) ->
                Body(
                        contentType = contentType,
                        examples = bodies.mapNotNull { it.example },
                        schema = bodies.mapNotNull { it.schema }
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
                        bodies = mergeBodiesWithSameContentType(responses
                                .flatMap { it.bodies }
                                .groupBy { it.contentType }, jsonSchemaMerger)
                )
            }
        }
    }
}

data class OpenAPIFragment(val id: String,
                           val path: String,
                           val method: Method,
                           val parameters: List<Parameter> = emptyList()) {

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
                    parameters = parameters(parameters),
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

        private fun body(map: Map<*,*>): Body {
            val contentType = map.keys.first() as String
            val values = map[contentType] as Map<*,*>
            return Body(
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
                    headers = headers((values["headers"] as? Map<*, *>).orEmpty()),
                    bodies = if (values["content"] == null) emptyList() else listOf(body(values["body"] as Map<*, *>))
            )
        }

        private fun method(map: Map<*,*>): Method {
            val methodContent = map[map.keys.first()] as Map<*,*>
            val response = methodContent["responses"] as? Map<*,*>
            return Method(
                    method = map.keys.first() as String,
                    description = methodContent["description"] as? String,
                    requestBodies = (methodContent["content"] as? Map<*, *>)?.let { listOf(body(it)) }.orEmpty(),
                    parameters = parameters((methodContent["parameters"] as? Map<*, *>).orEmpty()),
                    responses = response?.let { listOf(response(it)) }.orEmpty()
            )
        }

        private fun parameters(map: Map<*,*>): List<Parameter> {
            return map.map { (key, value) -> with(value as Map<*, *>) {
                Parameter(key as String, value["description"] as String, value["type"] as String, value["required"] as String, value["type"] as String)
            } }
        }

        private fun headers(map: Map<*,*>): List<Header> {
            return map.map { (key, value) -> with(value as Map<*, *>) {
                Header(key as String, value["description"] as String, value["example"] as String)
            } }
        }
    }
}
