package cc.dille.restdocs.openapi

import org.gradle.api.Plugin
import org.gradle.api.Project


class RestdocsOpenAPIPlugin: Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            extensions.create("apidoc", RestdocsOpenAPIPluginExtension::class.java, project)
            afterEvaluate {
                val openAPIdoc = extensions.findByName("apidoc") as RestdocsOpenAPIPluginExtension
                tasks.create("apidoc", RestdocsOpenAPITask::class.java).apply {
                    dependsOn("check")
                    description = "Aggregate fragments into an OpenAPIResource file"

                    openAPIVersion = openAPIdoc.openAPIVersion
                    apiBaseUri = openAPIdoc.apiBaseUri
                    apiTitle = openAPIdoc.apiTitle

                    separatePublicApi = openAPIdoc.separatePublicApi

                    outputDirectory = openAPIdoc.outputDirectory
                    snippetsDirectory = openAPIdoc.snippetsDirectory

                    outputFileNamePrefix = openAPIdoc.outputFileNamePrefix
                }
            }
        }
    }

}
