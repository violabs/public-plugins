package io.violabs.plugins.local.publishing.digitalocean.task

import io.violabs.plugins.local.publishing.digitalocean.adapter.DefaultProjectAdapter
import io.violabs.plugins.local.publishing.digitalocean.domain.DigitalOceanSpacesExtension
import io.violabs.plugins.local.publishing.digitalocean.service.CheckVersionDigitalOceanSpacesService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import software.amazon.awssdk.services.s3.S3Client

/**
 * Task to check if a specific version of an artifact already exists in Digital Ocean Spaces.
 * If the version exists, it throws an exception; otherwise, it logs a notice message.
 * This task is intended to be used in a Gradle build script to prevent accidental overwriting
 * of existing versions in Digital Ocean Spaces.
 */
abstract class DigitalOceanSpacesCheckVersionTask : DefaultTask() {
    /**
     * The extension for configuring Digital Ocean Spaces access information.
     * This property is used to retrieve the access key, secret key, bucket name,
     * endpoint, and region for Digital Ocean Spaces.
     * It is expected to be set in the build script using the `digitalOceanSpaces` extension.
     */
    @get:Input
    abstract val extension: Property<DigitalOceanSpacesExtension>

    @get:Input
    abstract var s3Client: S3Client

    /**
     * Whether to continue execution when version check fails (default: false)
     */
    @get:Input
    abstract val continueOnFailure: Property<Boolean>

    /**
     * Whether to suppress detailed exception information (default: true)
     */
    @get:Input
    abstract val suppressDetailedExceptions: Property<Boolean>

    init {
        continueOnFailure.convention(true)
        suppressDetailedExceptions.convention(true)
    }

    /**
     * Checks if the current project version already exists in Digital Ocean Spaces.
     * If the version exists, it throws a [org.gradle.api.GradleException] with an error message.
     * If the version does not exist, it logs a notice message.
     * This task is intended to be used in a Gradle build script to prevent
     * accidental overwriting of existing versions in Digital Ocean Spaces.
     * This task requires the following properties to be set in the extension:
     * - `accessKey`: The access key for Digital Ocean Spaces.
     * - `secretKey`: The secret key for Digital Ocean Spaces.
     * - `bucket`: The name of the Digital Ocean Spaces bucket.
     * - `endpoint`: The endpoint URL for Digital Ocean Spaces (default is "https://nyc3.digitaloceanspaces.com").
     * - `region`: The region for Digital Ocean Spaces (default is "nyc3").
     * * The task will also append metadata to the GITHUB_OUTPUT file if it is available.
     * This metadata includes the project version, name, and tag.
     * This is useful for GitHub Actions to capture the version information.
     * @throws org.gradle.api.GradleException if the version already exists in Digital Ocean Spaces.
     * @throws IllegalArgumentException if any of the required access information is missing.
     */
    @TaskAction
    fun checkVersion() {
        CheckVersionDigitalOceanSpacesService().checkVersion(
            DefaultProjectAdapter(project),
            extension.get(),
            s3Client
        )
    }
}