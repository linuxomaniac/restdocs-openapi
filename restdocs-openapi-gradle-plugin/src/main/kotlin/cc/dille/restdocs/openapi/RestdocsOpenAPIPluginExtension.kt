package cc.dille.restdocs.openapi

import org.gradle.api.Project

open class RestdocsOpenAPIPluginExtension(project: Project) {
    var openAPIVersion = "3.0.1"

    var infoVersion = "0.1.0"
    var infoTitle = "API documentation"
    var infoDescription: String? = null
    var infoContactName: String? = null
    var infoContactEmail: String? = null

    var serverUrl: String? = null
    var serverDescription: String? = null

    var mergeFiles = false

    var outputDirectory = "build/openAPIdoc"
    var snippetsDirectory = "build/generated-snippets"

    var outputFileNamePrefix = "api"
}
