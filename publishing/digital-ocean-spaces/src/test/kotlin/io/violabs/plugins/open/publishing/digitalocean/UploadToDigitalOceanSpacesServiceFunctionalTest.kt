package io.violabs.plugins.open.publishing.digitalocean

import io.mockk.*
import io.violabs.plugins.open.publishing.digitalocean.adapter.ProjectAdapter
import io.violabs.plugins.open.publishing.digitalocean.adapter.S3BuilderAdapter
import io.violabs.plugins.open.publishing.digitalocean.client.DefaultDigitalOceanSpacesClient
import io.violabs.plugins.open.publishing.digitalocean.domain.DigitalOceanSpacesExtension
import io.violabs.plugins.open.publishing.digitalocean.service.CheckVersionDigitalOceanSpacesService
import io.violabs.plugins.open.publishing.digitalocean.service.UploadToDigitalOceanSpacesService
import io.violabs.test.core.UnitTest
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.io.File
import java.nio.file.Path

class UploadToDigitalOceanSpacesServiceFunctionalTest : UnitTest() {
    val logger: Logger = Logging.getLogger(UploadToDigitalOceanSpacesServiceFunctionalTest::class.java)

    val s3Client = mockk<S3Client> {
        every { close() } just Runs
    }

    val s3BuilderAdapter = mockk<S3BuilderAdapter> {
        every { build() } returns s3Client
    }

    val mockVersionCheck: CheckVersionDigitalOceanSpacesService = mockk()

    val testBucket = "test-bucket"
    val givenArtifactPath = "io/violabs/my-lib/1.0.0"
    val expectedKeyPath = "my-lib-1.0.0"
    val sharedAccessKey = "test access"
    val sharedSecretKey = "test secret"

    val digitalOceanSpacesExtension = DigitalOceanSpacesExtension().apply {
        accessKey = sharedAccessKey
        secretKey = sharedSecretKey
        bucket = testBucket
        artifactPath = givenArtifactPath
    }

    val digitalOceanSpacesClient = DefaultDigitalOceanSpacesClient(
        digitalOceanSpacesExtension,
        logger
    ) { _, _ -> s3BuilderAdapter }

    // 3 error throws
    @Test
    fun `uploadSpaces throws an exception if access key is missing`(@TempDir tempDir: Path) {
        val digitalOceanSpacesExtension = DigitalOceanSpacesExtension().apply {
            accessKey = null
            secretKey = sharedSecretKey
            bucket = testBucket
            artifactPath = givenArtifactPath
        }

        exceptionTest(tempDir, digitalOceanSpacesExtension, "accessKey is required")
    }

    @Test
    fun `uploadSpaces throws an exception if secret key is missing`(@TempDir tempDir: Path) {
        val digitalOceanSpacesExtension = DigitalOceanSpacesExtension().apply {
            accessKey = sharedAccessKey
            secretKey = null
            bucket = testBucket
            artifactPath = givenArtifactPath
        }

        exceptionTest(tempDir, digitalOceanSpacesExtension, "secretKey is required")
    }

    @Test
    fun `uploadSpaces throws an exception if bucket is missing`(@TempDir tempDir: Path) {
        val digitalOceanSpacesExtension = DigitalOceanSpacesExtension().apply {
            accessKey = sharedAccessKey
            secretKey = sharedSecretKey
            bucket = null
            artifactPath = givenArtifactPath
        }

        exceptionTest(tempDir, digitalOceanSpacesExtension, "bucket is required")
    }

    @Test
    fun `uploadSpaces throws an exception if artifactPath is missing`(@TempDir tempDir: Path) {
        val digitalOceanSpacesExtension = DigitalOceanSpacesExtension().apply {
            accessKey = sharedAccessKey
            secretKey = sharedSecretKey
            bucket = testBucket
            artifactPath = null
        }

        exceptionTest(tempDir, digitalOceanSpacesExtension, "artifactPath is required")
    }

    private fun exceptionTest(tempDir: Path, extension: DigitalOceanSpacesExtension, expectedMessage: String) {
        val project = TestProjectAdapter(tempDir)

        val digitalOceanSpacesClient = DefaultDigitalOceanSpacesClient(extension, logger) { _, _ -> s3BuilderAdapter }

        val uploadService = UploadToDigitalOceanSpacesService(
            project,
            digitalOceanSpacesClient,
            s3Client,
            isPlugin = false,
            mockVersionCheck
        )

        every { mockVersionCheck.checkVersion(any(), any(), any()) } just Runs

        assertThrows<IllegalArgumentException> {
            uploadService.uploadToSpaces()
        }.apply {
            assert(message?.contains(expectedMessage) == true)
        }

        verify { mockVersionCheck.checkVersion(any(), any(), any()) }

        confirmVerified(s3Client, mockVersionCheck)
    }

    @Test
    fun `uploadSpaces standard library happy path`(@TempDir tempDir: Path) {
        // Create project adapter inside the test method
        val project = TestProjectAdapter(tempDir)

        // Setup file structure
        createRequiredFileStructure(tempDir.toFile())

        val uploadService = UploadToDigitalOceanSpacesService(
            project,
            digitalOceanSpacesClient,
            s3Client,
            isPlugin = false,
            mockVersionCheck
        )

        val jar = TestScenario(testBucket, givenArtifactPath, expectedKeyPath)
        val sources = jar.copy(prefix = "sources")
        val javadoc = jar.copy(prefix = "javadoc")
        val kdoc = jar.copy(prefix = "kdoc")
        val pom = jar.copy(fileType = "pom")

        every { mockVersionCheck.checkVersion(any(), any(), any()) } just Runs

        everyFileWithShas(jar)
        everyFileWithShas(sources)
        everyFileWithShas(javadoc)
        everyFileWithShas(kdoc)
        everyFileWithShas(pom)

        uploadService.uploadToSpaces()

        verifyFilesWithShas(jar)
        verifyFilesWithShas(sources)
        verifyFilesWithShas(javadoc)
        verifyFilesWithShas(kdoc)
        verifyFilesWithShas(pom)

        verify(atLeast = 1) { s3Client.close() }

        verify { mockVersionCheck.checkVersion(any(), any(), any()) }

        confirmVerified(s3Client, mockVersionCheck)
    }

    @Test
    fun `uploadSpaces plugin library happy path`(@TempDir tempDir: Path) {
        // Create project adapter inside the test method
        val project = TestProjectAdapter(tempDir)

        // Setup file structure
        createRequiredFileStructure(tempDir.toFile(), isPlugin = true)

        val uploadService = UploadToDigitalOceanSpacesService(
            project,
            digitalOceanSpacesClient,
            s3Client,
            isPlugin = true,
            mockVersionCheck
        )

        val jar = TestScenario(testBucket, givenArtifactPath, expectedKeyPath)
        val sources = jar.copy(prefix = "sources")
        val javadoc = jar.copy(prefix = "javadoc")
        val kdoc = jar.copy(prefix = "kdoc")
        val pom = jar.copy(fileType = "pom")

        every { mockVersionCheck.checkVersion(any(), any(), any()) } just Runs

        everyFileWithShas(jar)
        everyFileWithShas(sources)
        everyFileWithShas(javadoc)
        everyFileWithShas(kdoc)
        everyFileWithShas(pom)
        every {
            s3Client.putObject(
                match<PutObjectRequest> {
                    it.bucket() == testBucket
                        && it.key() == "plugins/io/violabs/my-lib/1.0.0/io.violabs.my-lib.gradle.plugin-1.0.0.pom"
                        && it.acl() == ObjectCannedACL.PUBLIC_READ
                },
                match<Path> {
                    it.endsWith("libs/io.violabs.my-lib.gradle.plugin-1.0.0.pom")
                }
            )
        } returns PutObjectResponse.builder().build()

        every {
            s3Client.putObject(
                match<PutObjectRequest> {
                    it.bucket() == testBucket
                        && it.key() == "plugins/io/violabs/my-lib/1.0.0/my-lib-1.0.0.jar"
                        && it.acl() == ObjectCannedACL.PUBLIC_READ
                },
                match<Path> {
                    it.endsWith("libs/my-lib-1.0.0.jar")
                }
            )
        } returns PutObjectResponse.builder().build()

        uploadService.uploadToSpaces()

        verifyFilesWithShas(jar)
        verifyFilesWithShas(sources)
        verifyFilesWithShas(javadoc)
        verifyFilesWithShas(kdoc)
        verifyFilesWithShas(pom)

        verify(atLeast = 1) { s3Client.close() }

        verify {
            s3Client.putObject(
                match<PutObjectRequest> {
                    it.bucket() == testBucket
                        && it.key() == "plugins/io/violabs/my-lib/1.0.0/io.violabs.my-lib.gradle.plugin-1.0.0.pom"
                        && it.acl() == ObjectCannedACL.PUBLIC_READ
                },
                match<Path> {
                    it.endsWith("libs/io.violabs.my-lib.gradle.plugin-1.0.0.pom")
                }
            )
        }

        verify {
            s3Client.putObject(
                match<PutObjectRequest> {
                    it.bucket() == testBucket
                        && it.key() == "plugins/io/violabs/my-lib/1.0.0/my-lib-1.0.0.jar"
                        && it.acl() == ObjectCannedACL.PUBLIC_READ
                },
                match<Path> {
                    it.endsWith("libs/my-lib-1.0.0.jar")
                }
            )
        }

        verify { mockVersionCheck.checkVersion(any(), any(), any()) }

        confirmVerified(s3Client, mockVersionCheck)
    }

    private class TestProjectAdapter(
        tempDir: Path,
        override val name: String = "my-lib",
        override val version: String = "1.0.0"
    ) : ProjectAdapter {
        override val project: Project = mockk()
        override val buildDir: File = tempDir.toFile()
        override val logger: Logger = Logging.getLogger(UploadToDigitalOceanSpacesServiceFunctionalTest::class.java)

        override fun pluginAdapters(): List<ProjectAdapter.MavenPublicationAdapter> {
            return listOf(
                object : ProjectAdapter.MavenPublicationAdapter {
                    override val groupId: String = "io.violabs"
                    override val artifactId: String = "my-lib"
                    override val version: String = "1.0.0"
                    override val name: String = "test"
                }
            )
        }
    }

    private fun everyFileWithShas(
        testScenario: TestScenario,
        postfixes: List<String?> = listOf(null, "sha1", "sha256")
    ) {
        val prefix = testScenario.prefix?.let { "-$it" } ?: ""
        val givenArtifactPath = testScenario.givenArtifactPath
        val expectedKeyPath = testScenario.expectedKeyPath
        val fileName = testScenario.pathOverride ?: "$expectedKeyPath$prefix.${testScenario.fileType}"
        val keyBase = "$givenArtifactPath/$fileName"

        for (item in postfixes) {
            val postfix = item?.let { ".$it" } ?: ""
            every {
                s3Client.putObject(
                    match<PutObjectRequest> {
                        it.bucket() == testScenario.bucket
                            && it.key() == "$keyBase$postfix"
                            && it.acl() == ObjectCannedACL.PUBLIC_READ
                    },
                    match<Path> {
                        val path = testScenario
                            .pathOverride
                            ?: "$expectedKeyPath$prefix.${testScenario.fileType}$postfix"
                        it.endsWith("libs/$path")
                    }
                )
            } returns PutObjectResponse.builder().build()
        }
    }

    private fun verifyFilesWithShas(
        testScenario: TestScenario,
        postfixes: List<String?> = listOf(null, "sha1", "sha256")
    ) {
        val prefix = testScenario.prefix?.let { "-$it" } ?: ""
        val givenArtifactPath = testScenario.givenArtifactPath
        val expectedKeyPath = testScenario.expectedKeyPath
        val fileName = testScenario.pathOverride ?: "$expectedKeyPath$prefix.${testScenario.fileType}"
        val keyBase = "$givenArtifactPath/$fileName"

        for (item in postfixes) {
            val postfix = item?.let { ".$it" } ?: ""
            verify {
                s3Client.putObject(
                    match<PutObjectRequest> {
                        it.bucket() == testScenario.bucket
                            && it.key() == "$keyBase$postfix"
                            && it.acl() == ObjectCannedACL.PUBLIC_READ
                    },
                    match<Path> {
                        val path = testScenario
                            .pathOverride ?: "$expectedKeyPath$prefix.${testScenario.fileType}$postfix"
                        it.endsWith("libs/$path")
                    }
                )
            }
        }
    }
}

private data class TestScenario(
    val bucket: String,
    val givenArtifactPath: String,
    val expectedKeyPath: String,
    val prefix: String? = null,
    val fileType: String = "jar",
    val pathOverride: String? = null
)