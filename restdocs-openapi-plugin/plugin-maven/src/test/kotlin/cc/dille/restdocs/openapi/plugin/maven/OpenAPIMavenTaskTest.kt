package cc.dille.restdocs.openapi.plugin.maven

import cc.dille.restdocs.openapi.plugin.common.OpenAPITaskTestResources
import org.apache.maven.it.VerificationException
import org.apache.maven.it.Verifier
import org.junit.Before
import org.junit.Test

class OpenAPIMavenTaskTest : OpenAPITaskTestResources() {
    private lateinit var verifier: Verifier
    private var success = false

    @Before
    fun gradleSetUp() {
        buildFile = testProjectDir.newFile("pom.xml")

        testProjectDir.newFolder("target", "generated-snippets")
    }

    override fun thenTaskSucceeded() =
            success

    override fun whenPluginExecuted() {
        verifier = Verifier(testProjectDir.root.absolutePath)
        try {
            verifier.executeGoal("openAPIdoc")
            success = true
        } catch (e: VerificationException) {
            success = false
        }
    }

    override fun baseBuildFile() = """plugins {
    id 'java'
    id 'cc.dille.restdocs-openapi'
}
""".trimIndent()

    override fun fullBuildFile(): String = baseBuildFile() + """
openAPIdoc {
    openAPIVersion = "$openAPIVersion"
    infoVersion = "$infoVersion"
    infoTitle = "$infoTitle"
    infoDescription = "$infoDescription"
    infoContactName = "$infoContactName"
    infoContactEmail = "$infoContactEmail"
    infoContactUrl = "$infoContactUrl"
    serverUrl = "$serverUrl"
    serverDescription = "$serverDescription"
    outputFileNamePrefix = "$outputFileNamePrefix"
}
"""

    // The actual tests
    @Test
    fun `call should aggregate openAPI fragments`() =
            `should aggregate openAPI fragments`()

    @Test
    fun `call should aggregate openAPI fragments with missing openAPIdoc closure`() =
            `should aggregate openAPI fragments with missing openAPIdoc closure`()
}
