package cc.dille.restdocs.openapi.plugin.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project


class RestdocsOpenAPIPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            extensions.create("openAPIdoc", RestdocsOpenAPIPluginExtension::class.java, project)
            afterEvaluate {
                val openAPIdoc = extensions.findByName("openAPIdoc") as RestdocsOpenAPIPluginExtension
                tasks.create("openAPIdoc", RestdocsOpenAPITask::class.java).apply {
                    dependsOn("check")
                    description = "Aggregate fragments into an OpenAPI file"

                    openAPIVersion = openAPIdoc.openAPIVersion

                    infoVersion = openAPIdoc.infoVersion
                    infoTitle = openAPIdoc.infoTitle
                    infoDescription = openAPIdoc.infoDescription
                    infoContactName = openAPIdoc.infoContactName
                    infoContactEmail = openAPIdoc.infoContactEmail
                    infoContactUrl = openAPIdoc.infoContactUrl

                    serverUrl = openAPIdoc.serverUrl
                    serverDescription = openAPIdoc.serverDescription

                    outputDirectory = openAPIdoc.outputDirectory
                    snippetsDirectory = openAPIdoc.snippetsDirectory

                    outputFileNamePrefix = openAPIdoc.outputFileNamePrefix
                }
            }
        }
    }

}
