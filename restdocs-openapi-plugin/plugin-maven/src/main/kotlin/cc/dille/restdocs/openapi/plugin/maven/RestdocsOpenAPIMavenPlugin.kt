package cc.dille.restdocs.openapi.plugin.maven

import cc.dille.restdocs.openapi.plugin.common.OpenAPIAggregate
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File


@Mojo(name = "openAPIdoc")
class RestdocsOpenAPIMavenPlugin : AbstractMojo() {
    @Parameter(property = "openAPIversion")
    var openAPIVersion = "3.0.1"

    @Parameter(property = "infoVersion")
    var infoVersion = "0.1.0"
    @Parameter(property = "infoTitle")
    var infoTitle = "API documentation"

    @Parameter(property = "infoDescription")
    var infoDescription: String? = null

    @Parameter(property = "infoContactName")
    var infoContactName: String? = null
    @Parameter(property = "infoContactEmail")
    var infoContactEmail: String? = null
    @Parameter(property = "infoContactUrl")
    var infoContactUrl: String? = null

    @Parameter(property = "serverUrl")
    var serverUrl: String? = null
    @Parameter(property = "serverDescription")
    var serverDescription: String? = null

    @Parameter(property = "outputDirectory")
    var outputDirectory = "target/openAPIdoc"
    @Parameter(property = "snippetsDirectory")
    var snippetsDirectory = "target/generated-snippets"

    @Parameter(property = "outputFileNamePrefix")
    var outputFileNamePrefix = "api"

    @Parameter(readonly = true, defaultValue = "\${project.build.directory}")
    var buildDir: String? = null

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
                File(buildDir!!)
        )
                .aggregateFragments()
    }

}
