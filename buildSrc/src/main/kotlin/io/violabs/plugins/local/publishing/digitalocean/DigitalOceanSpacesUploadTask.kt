package io.violabs.plugins.local.publishing.digitalocean

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import software.amazon.awssdk.services.s3.S3Client
import java.io.File
import javax.inject.Inject

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

    @get:Input
    abstract val jarQualifier: Property<String>

    @get:Input
    abstract val version: Property<String>

    @get:Internal  // Not part of task inputs since it's behavior, not data
    var uploadService: UploadToDigitalOceanSpacesService? = null

    private val extension: DigitalOceanSpacesExtension
        get() = project.extensions.getByType(DigitalOceanSpacesExtension::class.java)


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
        val service = uploadService ?: UploadToDigitalOceanSpacesService(
            project,
            digitalOceanSpacesClient,
            checkS3Client,
            jarQualifier.getOrNull(),
            isPlugin = extension.isPlugin
        )

        service.uploadToSpaces()
    }
}