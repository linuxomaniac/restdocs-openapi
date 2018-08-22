package cc.dille.restdocs.openapi.plugin.maven

import cc.dille.restdocs.openapi.plugin.common.OpenAPIAggregate
import cc.dille.restdocs.openapi.plugin.common.PluginDefaultValues
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File


@Mojo(name = "openapidoc")
class RestdocsOpenAPIMavenPlugin : AbstractMojo() {
    private val o = PluginDefaultValues()

    @Parameter(property = "openAPIversion")
    var openAPIVersion: String = o.openAPIVersion

    @Parameter(property = "infoVersion")
    var infoVersion : String = o.infoVersion
    @Parameter(property = "infoTitle")
    var infoTitle: String = o.infoTitle

    @Parameter(property = "infoDescription")
    var infoDescription: String? = o.infoDescription

    @Parameter(property = "infoContactName")
    var infoContactName: String? = o.infoContactName
    @Parameter(property = "infoContactEmail")
    var infoContactEmail: String? = o.infoContactEmail
    @Parameter(property = "infoContactUrl")
    var infoContactUrl: String? = o.infoContactUrl

    @Parameter(property = "serverUrl")
    var serverUrl: String? = o.serverUrl
    @Parameter(property = "serverDescription")
    var serverDescription: String? = o.serverDescription

    @Parameter(property = "outputDirectory")
    var outputDirectory: String = o.outputDirectory
    @Parameter(property = "snippetsDirectory")
    var snippetsDirectory: String = o.snippetsDirectory

    @Parameter(property = "outputFileNamePrefix")
    var outputFileNamePrefix = o.outputFileNamePrefix

    @Parameter(readonly = true, defaultValue = "\${project.build.directory}")
    lateinit var buildDir: String

    override fun execute() {
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
                File(buildDir)
        )
                .aggregateFragments()
    }

}
