package cc.dille.restdocs.openapi.plugin.gradle

import cc.dille.restdocs.openapi.plugin.common.OpenAPIAggregate
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction


open class RestdocsOpenAPIGradleTask : DefaultTask() {

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


    @TaskAction
    fun aggregateOpenAPIFragments() {
        OpenAPIAggregate(openAPIVersion,
                infoVersion,
                infoTitle,
                infoDescription,
                infoContactName,
                infoContactEmail,
                infoContactUrl,
                serverUrl,
                serverDescription,
                outputDirectory,
                snippetsDirectory,
                outputFileNamePrefix,
                project.buildDir
        )
                .aggregateFragments()
    }
}
