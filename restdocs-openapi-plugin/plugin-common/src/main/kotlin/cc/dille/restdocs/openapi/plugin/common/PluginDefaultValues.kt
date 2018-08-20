package cc.dille.restdocs.openapi.plugin.common

open class PluginDefaultValues {
    var openAPIVersion = "3.0.1"

    var infoVersion = "0.1.0"
    var infoTitle = "API documentation"
    var infoDescription: String? = null
    var infoContactName: String? = null
    var infoContactEmail: String? = null
    var infoContactUrl: String? = null

    var serverUrl: String? = null
    var serverDescription: String? = null

    var outputDirectory = "openAPIdoc"
    var snippetsDirectory = "generated-snippets"

    var outputFileNamePrefix = "api"
}
