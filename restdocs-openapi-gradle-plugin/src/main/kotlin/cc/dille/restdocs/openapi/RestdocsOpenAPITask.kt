package cc.dille.restdocs.openapi

import cc.dille.restdocs.openapi.OpenAPIVersion.V_3_0_1
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.StandardCopyOption


open class RestdocsOpenAPITask: DefaultTask() {

    @Input
    lateinit var openAPIVersion: String

    @Input
    @Optional
    var apiBaseUri: String? = null

    @Input
    @Optional
    lateinit var apiTitle: String

    @Input
    var separatePublicApi: Boolean = false

    @Input
    lateinit var outputDirectory: String

    @Input
    lateinit var snippetsDirectory: String

    @Input
    lateinit var outputFileNamePrefix: String

    private val outputDirectoryFile
        get() = project.file(outputDirectory)

    private val snippetsDirectoryFile
        get() = project.file(snippetsDirectory)


    @TaskAction
    fun aggregateOpenAPIFragments() {
        outputDirectoryFile.mkdirs()

        copyBodyJsonFilesToOutput()

        val openAPIFragments = snippetsDirectoryFile.walkTopDown()
                .filter { it.name is String && it.name.startsWith("openapi-resource") }
                .map { OpenAPIFragment.fromFile(it) }
                .toList()

        writeFiles(openAPIFragments, ".yaml")
    }


    private fun writeFiles(openAPIFragments: List<OpenAPIFragment>, fileNameSuffix: String) {

        val openAPIApi = openAPIFragments.groupBy { it.path }
                .map { (_, fragmentsWithSamePath) -> OpenAPI.fromFragments(fragmentsWithSamePath, JsonSchemaMerger(outputDirectoryFile)) }
                .let { openAPIResources -> openAPIResources
                        .groupBy { it.firstPathPart }
                        .map { (firstPathPart, resources) -> ResourceGroup(firstPathPart, resources) } }
                .let { OpenAPIApi(apiTitle, apiBaseUri, openAPIVersion(), it) }

        OpenAPIWriter.writeApi(
                fileFactory = { filename -> project.file("$outputDirectory/$filename") },
                api = openAPIApi,
                apiFileName = "$outputFileNamePrefix$fileNameSuffix",
                groupFileNameProvider = { path -> groupFileName(path, fileNameSuffix) }
        )
    }

    private fun groupFileName(path: String, fileNameSuffix: String): String {
        val fileNamePrefix = if (path == "/") "root" else path
                .replaceFirst("/", "")
                .replace("\\{", "")
                .replace("}", "")
                .replace("/", "-")

        return if (fileNamePrefix == outputFileNamePrefix) "$fileNamePrefix-group$fileNameSuffix"
        else "$fileNamePrefix$fileNameSuffix"
    }

    private fun openAPIVersion() = V_3_0_1

    private fun copyBodyJsonFilesToOutput() {
        snippetsDirectoryFile.walkTopDown().forEach {
                    if (it.name.endsWith("-request.json") || it.name.endsWith("-response.json"))
                        Files.copy(it.toPath(), outputDirectoryFile.toPath().resolve(it.name), StandardCopyOption.REPLACE_EXISTING)
                }
    }
}
