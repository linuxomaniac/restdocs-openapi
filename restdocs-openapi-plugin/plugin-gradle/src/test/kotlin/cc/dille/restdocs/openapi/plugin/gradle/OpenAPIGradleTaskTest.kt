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
            (result.task(":openapidoc")?.outcome == SUCCESS)

    override fun whenPluginExecuted() {
        result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("--info", "--stacktrace", "openapidoc")
                .withPluginClasspath(pluginClasspath)
//                .forwardOutput()
                .build()
    }

    override fun baseBuildFile() = """plugins {
    id 'java'
    id 'cc.dille.restdocs-openapi'
}
""".trimIndent()

    override fun fullBuildFile(): String = baseBuildFile() + """
openapidoc {
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
    fun `call should aggregate OpenAPI fragments`() =
            `should aggregate OpenAPI fragments`()

    @Test
    fun `call should aggregate OpenAPI fragments with missing openAPIDoc closure`() =
            `should aggregate OpenAPI fragments with missing openAPIDoc closure`()
}
