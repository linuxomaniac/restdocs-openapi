package cc.dille.restdocs.openapi.plugin.gradle

import cc.dille.restdocs.openapi.plugin.common.PluginDefaultValues
import org.gradle.api.Plugin
import org.gradle.api.Project


class RestdocsOpenAPIGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            extensions.create("openAPIdoc", PluginDefaultValues::class.java)
            afterEvaluate {
                val openAPIdoc = extensions.findByName("openAPIdoc") as PluginDefaultValues
                tasks.create("openAPIdoc", RestdocsOpenAPIGradleTask::class.java).apply {
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
