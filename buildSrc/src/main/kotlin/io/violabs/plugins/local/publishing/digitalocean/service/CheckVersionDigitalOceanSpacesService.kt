package io.violabs.plugins.local.publishing.digitalocean.service

import io.violabs.plugins.local.publishing.digitalocean.adapter.ProjectAdapter
import io.violabs.plugins.local.publishing.digitalocean.domain.DigitalOceanSpacesExtension
import org.gradle.api.GradleException
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import java.io.File

class CheckVersionDigitalOceanSpacesService : BuildService<CheckVersionDigitalOceanSpacesService.Params> {
    interface Params : BuildServiceParameters

    override fun getParameters(): Params = object : Params {}

    fun checkVersion(
        project: ProjectAdapter,
        ext: DigitalOceanSpacesExtension,
        s3Client: S3Client
    ) {
        val bucket = requireNotNull(ext.bucket) { "bucket is required" }

        addMetadataIfOutputFileIsAvailable(project)

        val key = createSpacesFileKey(project, ext)
        val request = buildRequest(project, ext)

        try {
            s3Client.processExistingVersion(project, bucket, key, request)
        } catch (_: NoSuchKeyException) {
            processNewVersion(project)
        }
    }

    /**
     * Adds metadata to the GITHUB_OUTPUT file if it is available.
     * The metadata includes the project version, name, and tag.
     * This is useful for GitHub Actions to capture the version information.
     * @param fileName The name of the output file, defaults to the GITHUB_OUTPUT environment variable.
     */
    private fun addMetadataIfOutputFileIsAvailable(
        project: ProjectAdapter,
        fileName: String? = System.getenv("GITHUB_OUTPUT")
    ) {
        if (fileName.isNullOrBlank()) {
            return
        }

        File(fileName).appendText(
            """
                version=${project.version}
                name=${project.name}
                tag=${project.name}-${project.version}
                """.trimIndent() + "\n"
        )
    }

    /**
     * Creates the key for the file in Digital Ocean Spaces.
     * Uses the artifactPath if provided, otherwise defaults to an empty string.
     * The key is structured as:
     * ```
     * artifactPath/projectName-projectVersion.jar
     * ```
     * @param ext The DigitalOceanSpacesExtension containing configuration information.
     * @return The key for the file in Digital Ocean Spaces.
     */
    private fun createSpacesFileKey(project: ProjectAdapter, ext: DigitalOceanSpacesExtension): String {
        return "${ext.artifactPath ?: ""}/${project.name}-${project.version}.jar"
    }

    /**
     * Builds the request to check if the object exists in Digital Ocean Spaces.
     * @param ext The DigitalOceanSpacesExtension containing configuration information.
     * @return The HeadObjectRequest to check for the object's existence.
     */
    private fun buildRequest(project: ProjectAdapter, ext: DigitalOceanSpacesExtension): HeadObjectRequest {
        return HeadObjectRequest.builder()
            .bucket(ext.bucket)
            .key(createSpacesFileKey(project, ext))
            .build()
    }

    /**
     * Attempts a check for an existing version in Digital Ocean Spaces.
     * If the version exists, it throws a [GradleException] with an error message.
     * If it does not exist, it throws [NoSuchKeyException]
     * @param bucket The name of the Digital Ocean Spaces bucket.
     * @param key The key of the object in the bucket.
     * @param request The HeadObjectRequest to check for the object's existence.
     */
    @Throws(exceptionClasses = [GradleException::class, NoSuchKeyException::class])
    private fun S3Client.processExistingVersion(
        project: ProjectAdapter,
        bucket: String,
        key: String,
        request: HeadObjectRequest
    ) {
        headObject(request)

        val errorMessage = """
                    ;Version ${project.version} already exists in Digital Ocean Spaces
                    ;  | [WARN] Artifact: ${project.name}
                    ;  | [WARN] Path: $bucket/$key
                    ;  | [WARN] Tag: ${project.name}-${project.version}
                    ;  | [WARN] Please update the version number in your build.gradle.kts file.
                """.trimMargin(";")

        throw GradleException(errorMessage)
    }

    /**
     * Processes the case where the version does not exist in Digital Ocean Spaces.
     * Logs a notice message with the version, artifact name, and tag.
     * This method is called when the version check passes successfully.
     */
    private fun processNewVersion(project: ProjectAdapter) {
        // Version doesn't exist
        project.logger.lifecycle(
            """
                    ;[INFO] notice::Version check passed
                    ;  | Version: ${project.version}
                    ;  | Artifact: ${project.name}
                    ;  | Tag: ${project.name}-${project.version}
                """.trimMargin(";")
        )
    }
}