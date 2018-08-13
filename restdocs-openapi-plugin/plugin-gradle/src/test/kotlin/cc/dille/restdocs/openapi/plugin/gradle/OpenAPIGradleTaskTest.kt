package cc.dille.restdocs.openapi.plugin.gradle

import cc.dille.restdocs.openapi.plugin.common.OpenAPITaskTestResources
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Before
import org.junit.Test

class OpenAPIGradleTaskTest : OpenAPITaskTestResources() {
    private lateinit var result: BuildResult

    @Before
    fun gradleSetUp() {
        buildFile = testProjectDir.newFile("build.gradle")

        testProjectDir.newFolder("build", "generated-snippets")
    }

    override fun thenTaskSucceeded() =
            (result.task(":openAPIdoc")?.outcome == SUCCESS)

    override fun whenPluginExecuted() {
        result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("--info", "--stacktrace", "openAPIdoc")
                .withPluginClasspath(pluginClasspath)
//                .forwardOutput()
                .build()
    }

    override fun baseBuildFile() = """plugins {
    id 'java'
    id 'cc.dille.restdocs-openapi'
}
""".trimIndent()

    override fun additionsToBuildFile(): String = """
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
