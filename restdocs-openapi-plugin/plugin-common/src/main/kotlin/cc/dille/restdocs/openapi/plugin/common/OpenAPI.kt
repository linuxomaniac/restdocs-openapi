package cc.dille.restdocs.openapi.plugin.common

import cc.dille.restdocs.openapi.plugin.common.OpenAPIParser.includeTag
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
import java.io.FileInputStream


object OpenAPIParser {

    val includeTag = Tag("!include")

    fun parseFragment(fragmentFile: File): Map<*, *> =
            yaml(fragmentFile.parent).load<Map<Any, Any>>(fragmentFile.inputStream())

    fun parseFragment(s: String, path: String? = null): Map<*, *> =
            yaml(path).load<Map<Any, Any>>(s)
}

object OpenAPIWriter {
    fun writeApi(fileFactory: (String) -> File, api: OpenAPIApi, apiFileName: String) {
        writeFile(targetFile = fileFactory(apiFileName),
                contentMap = api.toMainFileMap()
        )
    }

    fun writeFile(targetFile: File, contentMap: Map<*, *>) {
        targetFile.writer().let { writer ->
            yaml().dump(contentMap, writer)
        }
    }
}

private fun yaml(path: String? = null) = Yaml(IncludeConstructor(path), IncludeRepresenter(),
        DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            defaultScalarStyle = PLAIN
            isAllowReadOnlyProperties = true
        })

data class Include(val location: String)

internal class IncludeRepresenter : Representer() {
    init {
        this.representers[Include::class.java] = RepresentInclude()
    }

    private inner class RepresentInclude : Represent {

        override fun representData(data: Any): Node {
            // We assume we stored the absolute location in the Include object (see ConstructInclude).
            val fip = FileInputStream(File((data as Include).location))
            val y = yaml().load<Any>(fip)

            return represent(y)
            // representScalar(includeTag, (data as Include).location)
        }
    }
}

internal class IncludeConstructor(path: String?) : SafeConstructor() {
    init {
        this.yamlConstructors[includeTag] = ConstructInclude(path)
    }

    private inner class ConstructInclude(path: String?) : AbstractConstruct() {
        val path: String? = path

        override fun construct(node: Node): Any {
            // We add here the absolute path to the file if path is not null.
            // The file will otherwise be relative.
            val value = path?.let { "$it/" }.orEmpty() + (constructScalar(node as ScalarNode) as String)
            return Include(value)
        }
    }
}
