package cc.dille.restdocs.openapi.plugin.common

import java.io.File


class OpenAPIAggregate(
        private var openAPIVersion: String,
        private var infoVersion: String,
        private var infoTitle: String,
        private var infoDescription: String?,
        private var infoContactName: String?,
        private var infoContactEmail: String?,
        private var infoContactUrl: String?,
        private var serverUrl: String?,
        private var serverDescription: String?,
        private var outputDirectory: String,
        private var snippetsDirectory: String,
        private var outputFileNamePrefix: String,
        private var buildDir: File) {

    var outputDirectoryFile: File = File(buildDir, outputDirectory)
    var snippetsDirectoryFile: File = File(this.buildDir, snippetsDirectory)


    fun aggregateFragments() {
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
                fileFactory = { filename -> File(File(buildDir, outputDirectory), filename) },
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
