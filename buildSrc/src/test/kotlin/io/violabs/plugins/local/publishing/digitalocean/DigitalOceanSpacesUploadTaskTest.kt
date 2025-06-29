//package io.violabs.plugins.local.publishing.digitalocean
//
//import io.mockk.*
//import org.gradle.api.Project
//import org.gradle.api.publish.PublishingExtension
//import org.gradle.testfixtures.ProjectBuilder
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import software.amazon.awssdk.services.s3.S3Client
//import java.io.File
//
//class DigitalOceanSpacesUploadTaskTest {
//    private lateinit var project: Project
//    private lateinit var task: DigitalOceanSpacesUploadTask
//
//    // Mocks
//    private val mockS3Client = mockk<S3Client>(relaxed = true)
//    private val mockDigitalOceanSpacesClient = mockk<DigitalOceanSpacesClient>(relaxed = true)
//
//    @BeforeEach
//    fun setup() {
//        project = ProjectBuilder.builder()
//            .withName("test-lib")
//            .build()
//        project.version = "1.2.3"
//
//        // Register the extension if your task expects it:
//        project.extensions.create("digitalOceanSpaces", DigitalOceanSpacesExtension::class.java)
//
//        // Register publishing extension for plugin marker path
//        project.extensions.create("publishing", PublishingExtension::class.java)
//
//        // Register the task
//        task = project.tasks.create("doUpload", DigitalOceanSpacesUploadTask::class.java)
//
//        // Assign properties (using Kotlin reflection)
//        task.jarQualifier = "custom"
//        task.checkS3Client = mockS3Client
//        task.digitalOceanSpacesClient = mockDigitalOceanSpacesClient
//        task.isPlugin = false
//
//        // Setup required directories and files
//        val buildDir = File(project.buildDir, "")
//        val libsDir = File(buildDir, "libs")
//        libsDir.mkdirs()
//        File(libsDir, "test-lib-1.2.3.jar").writeText("main jar")
//        File(libsDir, "test-lib-1.2.3-sources.jar").writeText("sources jar")
//        File(libsDir, "test-lib-1.2.3-javadoc.jar").writeText("javadoc jar")
//        val publicationsDir = File(buildDir, "publications/maven")
//        publicationsDir.mkdirs()
//        File(publicationsDir, "pom-default.xml").writeText("<project/>")
//    }
//
//    @Test
//    fun `uploadToSpaces uploads files and calls DigitalOcean client`() {
//        every { DigitalOceanSpacesCheckVersionTask.checkVersion(any(), any(), any()) } just Runs
//        every { mockDigitalOceanSpacesClient.ext } returns DigitalOceanSpacesExtension()
//        every { mockDigitalOceanSpacesClient.uploadFile(any()) } just Runs
//
//        // Act
//        task.uploadToSpaces()
//
//        // Assert expected interactions
//        verify(atLeast = 1) {
//            mockDigitalOceanSpacesClient.uploadFile(match {
//                it.name.endsWith(".jar") || it.name.endsWith(
//                    ".pom"
//                )
//            })
//        }
//    }
//
//    @Test
//    fun `uploadToSpaces skips when version check fails`() {
//        every { DigitalOceanSpacesCheckVersionTask.checkVersion(any(), any(), any()) } throws RuntimeException("fail")
//        every { mockDigitalOceanSpacesClient.ext } returns DigitalOceanSpacesExtension()
//
//        // Should NOT call uploadFile if checkVersion fails
//        task.uploadToSpaces()
//
//        verify(exactly = 0) { mockDigitalOceanSpacesClient.uploadFile(any()) }
//    }
//
//}