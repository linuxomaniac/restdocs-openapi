package cc.dille.restdocs.openapi

import cc.dille.restdocs.openapi.OpenAPIParser.includeTag
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.DumperOptions.ScalarStyle.PLAIN
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.AbstractConstruct
import org.yaml.snakeyaml.constructor.SafeConstructor
import org.yaml.snakeyaml.nodes.Node
import org.yaml.snakeyaml.nodes.ScalarNode
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.representer.Represent
import org.yaml.snakeyaml.representer.Representer
import java.io.File
import java.io.InputStream


object OpenAPIParser {

    val includeTag = Tag("!include")

    fun parseFragment(fragmentFile: File): Map<*, *> = parseFragment(fragmentFile.inputStream())

    fun parseFragment(fragmentStream: InputStream): Map<*, *> = yaml()
            .load<Map<Any, Any>>(fragmentStream)

    fun parseFragment(s: String): Map<*, *> = yaml()
            .load<Map<Any, Any>>(s)
}

object OpenAPIWriter {
    fun writeApi(fileFactory: (String) -> File, api: OpenAPIApi, apiFileName: String, groupFileNameProvider: (String) -> String) {
        writeFile(targetFile = fileFactory(apiFileName),
                contentMap = api.toMainFileMap(groupFileNameProvider)
                )

        api.resourceGroups.map {
            writeFile(
                    targetFile = fileFactory(groupFileNameProvider(it.firstPathPart)),
                    contentMap = it.toOpenAPIMap())
        }
    }

    fun writeFile(targetFile: File, contentMap: Map<*, *>) {
        targetFile.writer().let { writer ->
            System.out.println(contentMap)
            yaml().dump(contentMap, writer)
        }
    }
}

private fun yaml() = Yaml(IncludeConstructor(), IncludeRepresenter(),
        DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            defaultScalarStyle = PLAIN
            isAllowReadOnlyProperties = true
        })

data class Include(val location: String) {
    fun toMap(): Map<String, String> = mapOf(includeTag.toString() to location)
    override fun toString(): String = location
}

internal class IncludeRepresenter : Representer() {
    init {
        this.representers[Include::class.java] = RepresentInclude()
    }

    private inner class RepresentInclude : Represent {
        override fun representData(data: Any): Node {
            return representScalar(includeTag, (data as Include).location)
        }
    }
}

internal class IncludeConstructor : SafeConstructor() {
    init {
        this.yamlConstructors[includeTag] = ConstructInclude()
    }

    private inner class ConstructInclude : AbstractConstruct() {

        override fun construct(node: Node): Any {
            val value = constructScalar(node as ScalarNode) as String
            return Include(value)
        }
    }
}
