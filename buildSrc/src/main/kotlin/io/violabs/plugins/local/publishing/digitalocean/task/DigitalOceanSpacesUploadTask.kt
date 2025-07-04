package io.violabs.plugins.local.publishing.digitalocean.task

import io.violabs.plugins.local.publishing.digitalocean.adapter.DefaultProjectAdapter
import io.violabs.plugins.local.publishing.digitalocean.client.DigitalOceanSpacesClient
import io.violabs.plugins.local.publishing.digitalocean.service.CheckVersionDigitalOceanSpacesService
import io.violabs.plugins.local.publishing.digitalocean.service.UploadToDigitalOceanSpacesService
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import software.amazon.awssdk.services.s3.S3Client

/**
 * Task to upload project artifacts to Digital Ocean Spaces.
 * This task uploads the main JAR, sources JAR, Javadoc JAR, and POM file
 * to the specified bucket in Digital Ocean Spaces.
 * It uses the configuration provided in the `DigitalOceanSpacesExtension`.
 */
abstract class DigitalOceanSpacesUploadTask : DefaultTask() {
    @get:Input
    abstract var checkS3Client: S3Client

    @get:Input
    abstract var digitalOceanSpacesClient: DigitalOceanSpacesClient

    @get:Internal  // Not part of task inputs since it's behavior, not data
    var uploadService: UploadToDigitalOceanSpacesService? = null

    /**
     * Uploads the project's artifacts to Digital Ocean Spaces.
     * This task uploads the main JAR, sources JAR, Javadoc JAR, and POM file
     * to the specified bucket in Digital Ocean Spaces.
     * It uses the configuration provided in the `DigitalOceanSpacesExtension`.
     * * The task requires the following properties to be set in the extension:
     * * - `accessKey`: The access key for Digital Ocean Spaces.
     * * - `secretKey`: The secret key for Digital Ocean Spaces.
     * * - `bucket`: The name of the Digital Ocean Spaces bucket.
     * * - `endpoint`: The endpoint URL for Digital Ocean Spaces (default is "https://nyc3.digitaloceanspaces.com").
     * * - `region`: The region for Digital Ocean Spaces (default is "nyc3").
     * * This task will upload the following files:
     * * - Main JAR file: `libs/<project-name>-<version>.jar`
     * * - Sources JAR file: `libs/<project-name>-<version>-sources.jar` (if it exists)
     * * - Javadoc JAR file: `libs/<project-name>-<version>-javadoc.jar` (if it exists)
     * * - POM file: `publications/maven/pom-default.xml` (if it exists)
     * * If any of these files do not exist, they will be skipped, and a warning will be logged.
     */
    @TaskAction
    fun uploadToSpaces() {
        val checkVersion = CheckVersionDigitalOceanSpacesService()

        val service = uploadService ?: UploadToDigitalOceanSpacesService(
            DefaultProjectAdapter(project),
            digitalOceanSpacesClient,
            checkS3Client,
            isPlugin = digitalOceanSpacesClient.ext.isPlugin,
            checkVersion
        )

        service.uploadToSpaces()
    }
}