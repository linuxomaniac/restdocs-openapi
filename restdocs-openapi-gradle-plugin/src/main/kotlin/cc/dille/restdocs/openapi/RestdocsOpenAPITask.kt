package cc.dille.restdocs.openapi

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.StandardCopyOption


open class RestdocsOpenAPITask : DefaultTask() {

    @Input
    lateinit var openAPIVersion: String

    @Input
    lateinit var infoVersion: String

    @Input
    lateinit var infoTitle: String

    @Input
    @Optional
    var infoDescription: String? = null

    @Input
    @Optional
    var infoContactName: String? = null

    @Input
    @Optional
    var infoContactEmail: String? = null

    @Input
    @Optional
    var infoContactUrl: String? = null

    @Input
    @Optional
    var serverUrl: String? = null

    @Input
    @Optional
    var serverDescription: String? = null

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

//        copyBodyJsonFilesToOutput()

        val openAPIFragments = snippetsDirectoryFile.walkTopDown()
                .filter { it.name is String && it.name.startsWith("openapi-resource") }
                .map { OpenAPIFragment.fromFile(it) }
                .toList()

        writeFiles(openAPIFragments, ".yaml")
    }


    private fun writeFiles(openAPIFragments: List<OpenAPIFragment>, fileNameSuffix: String) {
        val openAPIApi = openAPIFragments.groupBy { it.path }
                .map { (_, fragmentsWithSamePath) -> OpenAPIResource.fromFragments(fragmentsWithSamePath, JsonSchemaMerger(outputDirectoryFile)) }
                .let { openAPIResources ->
                    openAPIResources
                            .groupBy { it.path }
                            .map { (path, resources) -> ResourceGroup(path, resources) }
                }
                .let {
                    OpenAPIApi(openAPIVersion,
                            infoVersion,
                            infoTitle,
                            infoDescription,
                            infoContactName,
                            infoContactEmail,
                            infoContactUrl,
                            serverUrl,
                            serverDescription,
                            it)
                }

        OpenAPIWriter.writeApi(
                fileFactory = { filename -> project.file("$outputDirectory/$filename") },
                api = openAPIApi,
                apiFileName = "$outputFileNamePrefix$fileNameSuffix"
        )
    }

    /* private fun copyBodyJsonFilesToOutput() {
        snippetsDirectoryFile.walkTopDown().forEach {file ->
            if (listOf("-request.json", "-response.json", "-merged-response.json", "-merged-request.json").any { file.name.endsWith(it) }) {
                Files.copy(file.toPath(), outputDirectoryFile.toPath().resolve(file.name), StandardCopyOption.REPLACE_EXISTING)
            }
        }
    } */
}
