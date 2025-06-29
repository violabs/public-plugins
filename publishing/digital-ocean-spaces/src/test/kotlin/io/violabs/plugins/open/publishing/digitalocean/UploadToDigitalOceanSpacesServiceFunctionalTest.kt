package io.violabs.plugins.open.publishing.digitalocean

import io.mockk.*
import io.violabs.plugins.open.publishing.digitalocean.adapter.ProjectAdapter
import io.violabs.plugins.open.publishing.digitalocean.adapter.S3BuilderAdapter
import io.violabs.plugins.open.publishing.digitalocean.client.DefaultDigitalOceanSpacesClient
import io.violabs.plugins.open.publishing.digitalocean.client.DigitalOceanSpacesClient
import io.violabs.plugins.open.publishing.digitalocean.domain.DigitalOceanSpacesExtension
import io.violabs.plugins.open.publishing.digitalocean.service.UploadToDigitalOceanSpacesService
import org.gradle.api.Project
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import software.amazon.awssdk.services.s3.S3Client
import java.io.File
import java.nio.file.Path

class UploadToDigitalOceanSpacesServiceFunctionalTest {
    val s3Client = mockk<S3Client>()
    val s3BuilderAdapter = mockk<S3BuilderAdapter>(relaxed = true) {
        every { build() } returns s3Client
    }
    val mockVersionCheck: (ProjectAdapter, DigitalOceanSpacesClient, S3Client) -> Unit = mockk(relaxed = true)

    // include the client and the check version task
    @Test
    fun `uploadSpaces standard library happy path`(@TempDir tempDir: Path) {
        // Create project adapter inside the test method
        val project = TestProjectAdapter(tempDir)

        // Setup file structure
        createRequiredFileStructure(tempDir.toFile())

        val digitalOceanSpacesExtension = DigitalOceanSpacesExtension()
        val digitalOceanSpacesClient = DefaultDigitalOceanSpacesClient(
            digitalOceanSpacesExtension,
            mockk(relaxed = true)
        ) { _, _ -> s3BuilderAdapter }

        val uploadService = UploadToDigitalOceanSpacesService(
            project,
            digitalOceanSpacesClient,
            s3Client,
            isPlugin = false,
            checkVersionFunction = mockVersionCheck
        )

        // When
        uploadService.uploadToSpaces()

        // Then
        verify { mockVersionCheck(project, digitalOceanSpacesClient, s3Client) }
        verify(exactly = 13) { digitalOceanSpacesClient.uploadFile(any<File>()) }
    }

//
//    @Test
//    fun `uploadSpaces for plugin uploads markers`() {
//        createRequiredFileStructure()
//        createPluginFileStructure()
//
//        val mockVersionCheck: (ProjectAdapter, DigitalOceanSpacesClient, S3Client) -> Unit =
//            mockk(relaxed = true)
//
//        // Mock the extension for path manipulation
//        val mockExt = mockk<DigitalOceanSpacesExtension>(relaxed = true)
//        every { digitalOceanSpacesClient.ext } returns mockExt
//        every { mockExt.artifactPath } returns "original/path"
//        every { mockExt.artifactPath = any() } just Runs
//
//        val uploadService = UploadToDigitalOceanSpacesService(
//            project,
//            digitalOceanSpacesClient,
//            checkS3Client,
//            jarQualifier = null,
//            isPlugin = true,
//            checkVersionFunction = mockVersionCheck
//        )
//
//        uploadService.uploadToSpaces()
//
//        // Verify plugin marker upload path was set
//        verify { mockExt.artifactPath = "plugins/com/example/my-lib/1.0.0" }
//
//        // Verify plugin marker files were uploaded
//        verify {
//            digitalOceanSpacesClient.uploadFile(
//                match<File> { it.name.contains("gradle.plugin") }
//            )
//        }
//    }
//
//    private fun createRequiredFileStructure(jarQualifier: String? = null) {
//        val buildDir = tempDir.toFile()
//
//        // Create libs directory
//        val libsDir = File(buildDir, "libs").apply { mkdirs() }
//
//        val baseName = jarQualifier ?: "my-lib"
//
//        // Create all required JAR files and their checksums
//        listOf("", "-sources", "-javadoc", "-kdoc").forEach { suffix ->
//            File(libsDir, "$baseName-1.0.0$suffix.jar").writeText("dummy jar content")
//            File(libsDir, "$baseName-1.0.0$suffix.jar.sha1").writeText("dummy-sha1")
//            File(libsDir, "$baseName-1.0.0$suffix.jar.sha256").writeText("dummy-sha256")
//        }
//
//        // Create publications directory and POM file
//        val publicationsDir = File(buildDir, "publications/maven").apply { mkdirs() }
//        File(publicationsDir, "pom-default.xml").writeText("""
//            <?xml version="1.0" encoding="UTF-8"?>
//            <project>
//                <groupId>com.example</groupId>
//                <artifactId>my-lib</artifactId>
//                <version>1.0.0</version>
//            </project>
//        """.trimIndent())
//    }
//
//    private fun createPluginFileStructure() {
//        val buildDir = tempDir.toFile()
//
//        // Create plugin publication directory
//        val pluginPublicationDir = File(buildDir, "publications/maven").apply { mkdirs() }
//        File(pluginPublicationDir, "pom-default.xml").writeText("""
//            <?xml version="1.0" encoding="UTF-8"?>
//            <project>
//                <groupId>com.example</groupId>
//                <artifactId>my-lib</artifactId>
//                <version>1.0.0</version>
//            </project>
//        """.trimIndent())
//
//        // Create the main JAR that would be uploaded as plugin marker
//        val libsDir = File(buildDir, "libs")
//        File(libsDir, "my-lib-1.0.0.jar").writeText("plugin jar content")
//    }


    private fun createRequiredFileStructure(buildDir: File, jarQualifier: String? = null) {
        // Create libs directory
        val libsDir = File(buildDir, "libs").apply { mkdirs() }

        val baseName = jarQualifier ?: "my-lib"

        // Create all required JAR files and their checksums
        listOf("", "-sources", "-javadoc", "-kdoc").forEach { suffix ->
            File(libsDir, "$baseName-1.0.0$suffix.jar").writeText("dummy jar content")
            File(libsDir, "$baseName-1.0.0$suffix.jar.sha1").writeText("dummy-sha1")
            File(libsDir, "$baseName-1.0.0$suffix.jar.sha256").writeText("dummy-sha256")
        }

        // Create publications directory and POM file
        val publicationsDir = File(buildDir, "publications/maven").apply { mkdirs() }
        File(publicationsDir, "pom-default.xml").writeText("""
            <?xml version="1.0" encoding="UTF-8"?>
            <project>
                <groupId>com.example</groupId>
                <artifactId>my-lib</artifactId>
                <version>1.0.0</version>
            </project>
        """.trimIndent())
    }

    private class TestProjectAdapter(tempDir: Path) : ProjectAdapter {
        override val project: Project = mockk()
        override val buildDir: File = tempDir.toFile()
        override val name: String = "my-lib"
        override val version: String = "1.0.0"

        override fun pluginAdapters(): List<ProjectAdapter.MavenPublicationAdapter> {
            return listOf(
                object : ProjectAdapter.MavenPublicationAdapter {
                    override val groupId: String = "com.example"
                    override val artifactId: String = "my-lib"
                    override val version: String = "1.0.0"
                    override val name: String = "maven"
                }
            )
        }
    }
}