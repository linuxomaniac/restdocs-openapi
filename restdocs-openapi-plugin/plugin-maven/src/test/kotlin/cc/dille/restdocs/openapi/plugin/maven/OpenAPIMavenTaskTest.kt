package cc.dille.restdocs.openapi.plugin.maven

import cc.dille.restdocs.openapi.plugin.common.OpenAPITaskTestResources
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.apache.maven.shared.invoker.InvocationResult
import org.junit.Before
import org.junit.Test
import java.util.*

class OpenAPIMavenTaskTest : OpenAPITaskTestResources() {
    private lateinit var result: InvocationResult

    @Before
    fun gradleSetUp() {
        buildFile = testProjectDir.newFile("pom.xml")

        testProjectDir.newFolder("target", "generated-snippets")
    }

    override fun thenTaskSucceeded() =
            (result.exitCode == 0)

    override fun whenPluginExecuted() {
        val request = DefaultInvocationRequest()
        request.baseDirectory = testProjectDir.root
        request.pomFile = buildFile
        request.goals = Collections.singletonList("restdocs-openapi:openAPIdoc")
        val invoker = DefaultInvoker()
        result = invoker.execute(request)
    }

    override fun baseBuildFile() = """<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"
         xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <modelVersion>4.0.0</modelVersion>
    <groupId>cc.dille</groupId>
    <artifactId>restdocs-openapi-maven-test</artifactId>
    <version>0.0.1</version>
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
    <pluginRepositories>
        <pluginRepository>
            <id>jcenter</id>
            <url>https://jcenter.bintray.com/</url>
        </pluginRepository>
    </pluginRepositories>
    <build>
        <directory>build/</directory>
        <plugins>
            <plugin>
                <groupId>cc.dille.restdocs</groupId>
                <artifactId>restdocs-openapi-plugin-maven</artifactId>
                <version>0.1.0</version>
            </plugin>
        </plugins>
    </build>
</project>
""".trimIndent()

    override fun fullBuildFile(): String = """<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"
         xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <modelVersion>4.0.0</modelVersion>
    <groupId>cc.dille</groupId>
    <artifactId>restdocs-openapi-maven-test</artifactId>
    <version>0.0.1</version>
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
    <pluginRepositories>
        <pluginRepository>
            <id>jcenter</id>
            <url>https://jcenter.bintray.com/</url>
        </pluginRepository>
    </pluginRepositories>
    <build>
        <directory>build/</directory>
        <plugins>
            <plugin>
                <groupId>cc.dille.restdocs</groupId>
                <artifactId>restdocs-openapi-plugin-maven</artifactId>
                <version>0.1.0</version>
                <configuration>
                    <openAPIVersion>$openAPIVersion</openAPIVersion>
                    <infoVersion>$infoVersion</infoVersion>
                    <infoTitle>$infoTitle</infoTitle>
                    <infoDescription>$infoDescription</infoDescription>
                    <infoContactName>$infoContactName</infoContactName>
                    <infoContactEmail>$infoContactEmail</infoContactEmail>
                    <infoContactUrl>$infoContactUrl</infoContactUrl>
                    <serverUrl>$serverUrl</serverUrl>
                    <serverDescription>$serverDescription</serverDescription>
                    <outputFileNamePrefix>$outputFileNamePrefix</outputFileNamePrefix>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
"""

    // The actual tests
    @Test
    fun `call should aggregate openAPI fragments`() =
            `should aggregate openAPI fragments`()

    @Test
    fun `call should aggregate openAPI fragments with missing openAPIdoc closure`() =
            `should aggregate openAPI fragments with missing openAPIdoc closure`()
}
