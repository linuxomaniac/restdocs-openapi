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
import java.io.FileInputStream
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
    fun writeApi(fileFactory: (String) -> File, api: OpenAPIApi, apiFileName: String, groupFileNameProvider: (String) -> String, mergeIncludes: Boolean = false) {
        api.resourceGroups.map {
            writeFile(
                    targetFile = groupFileNameProvider(it.firstPathPart),
                    contentMap = it.toOpenAPIMap(),
                    fileFactory = fileFactory,
                    mergeIncludes = mergeIncludes
            )
        }

        writeFile(targetFile = apiFileName,
                contentMap = api.toMainFileMap(groupFileNameProvider),
                fileFactory = fileFactory,
                mergeIncludes = mergeIncludes
        )
    }

    fun writeFile(targetFile: String, contentMap: Map<*, *>, fileFactory: (String) -> File, mergeIncludes: Boolean) {
        fileFactory(targetFile).writer().let { writer ->
            yaml(fileFactory, mergeIncludes).dump(contentMap, writer)
        }
    }
}

private fun yaml(fileFactory: ((String) -> File)? = null, mergeIncludes: Boolean? = null) = Yaml(IncludeConstructor(), IncludeRepresenter(fileFactory, mergeIncludes),
        DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            defaultScalarStyle = PLAIN
            isAllowReadOnlyProperties = true
        })

data class Include(val location: String)

internal class IncludeRepresenter(fileFactory: ((String) -> File)?, mergeIncludes: Boolean?) : Representer() {
    private var fileFactory: ((String) -> File)?
    private var mergeIncludes: Boolean

    init {
        this.representers[Include::class.java] = RepresentInclude()
        this.fileFactory = fileFactory
        this.mergeIncludes = mergeIncludes?:false
    }

    private inner class RepresentInclude : Represent {
        override fun representData(data: Any): Node {
            return if(mergeIncludes && fileFactory != null) {
                val file = fileFactory!!.invoke((data as Include).location)
                val fip = FileInputStream(file)
                val y = yaml(fileFactory, mergeIncludes).load<Any>(fip)
                represent(y)
            } else {
                representScalar(includeTag, (data as Include).location)
            }
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
