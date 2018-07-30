package cc.dille.restdocs.openapi

import org.gradle.api.Plugin
import org.gradle.api.Project


class RestdocsOpenAPIPlugin: Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            extensions.create("openAPIdoc", RestdocsOpenAPIPluginExtension::class.java, project)
            afterEvaluate {
                val openAPIdoc = extensions.findByName("openAPIdoc") as RestdocsOpenAPIPluginExtension
                tasks.create("openAPIdoc", RestdocsOpenAPITask::class.java).apply {
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
