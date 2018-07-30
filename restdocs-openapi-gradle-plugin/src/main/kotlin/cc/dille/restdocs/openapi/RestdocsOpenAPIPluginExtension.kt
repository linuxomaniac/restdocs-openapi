package cc.dille.restdocs.openapi

import org.gradle.api.Project

open class RestdocsOpenAPIPluginExtension(project: Project) {
    var openAPIVersion = "3.0.1"
    var apiBaseUri: String? = null
    var apiTitle = "API documentation"

    var separatePublicApi: Boolean = false
    var outputDirectory = "build/openAPIdoc"
    var snippetsDirectory = "build/generated-snippets"

    var outputFileNamePrefix = "api"
}
