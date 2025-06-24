package io.violabs.plugins.open.publishing.digitalocean

import io.violabs.plugins.open.publishing.ManualMavenArtifactsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

/**
 * Plugin to manage publishing jars to digital ocean spaces.
 */
class DigitalOceanSpacesPublishPlugin : Plugin<Project> {
    /**
     * Applies the extension and registers tasks for Digital Ocean Spaces publishing.
     * @param project The Gradle project to which this plugin is applied.]
     */
    override fun apply(project: Project) = project.run {
        // Apply the maven-generated-artifacts plugin automatically
        pluginManager.apply("io.violabs.plugins.local.publishing.maven-generated-artifacts")
        pluginManager.apply("io.violabs.plugins.local.secrets.loader")

        // Create the Digital Ocean Spaces extension
        val doSpacesExtension = project.extensions.create<DigitalOceanSpacesExtension>("digitalOceanSpacesPublishing")

        // Get reference to the ManualMavenArtifactsExtension that was created by the applied plugin
        val mavenArtifactsExtension = project.extensions.getByType(ManualMavenArtifactsExtension::class.java)

        // Expose the Maven artifacts extension as a nested configuration block
        doSpacesExtension.mavenArtifacts = mavenArtifactsExtension

        project.afterEvaluate {
            project.logger.lifecycle("Applying DigitalOceanSpacesPublishPlugin to project: ${project.name}")
            project.logger.lifecycle(" | [INFO] endpoint: ${doSpacesExtension.endpoint}")
            project.logger.lifecycle(" | [INFO] bucket: ${doSpacesExtension.bucket}")
            project.logger.lifecycle(" | [INFO] region: ${doSpacesExtension.region}")
            project.logger.lifecycle(" | [INFO] artifactPath: ${doSpacesExtension.artifactPath}")
            project.logger.lifecycle(" | [INFO] dryRun: ${doSpacesExtension.dryRun}")

            // Log Maven artifacts configuration
            project.logger.lifecycle(" | [INFO] withDokka: ${mavenArtifactsExtension.withDokka}")
            project.logger.lifecycle(" | [INFO] artifact name: ${mavenArtifactsExtension.name ?: project.name}")

            logger.lifecycle(" | [INFO] Registering `checkDigitalOceanSpacesVersion` task")

            val doSpacesClient = DefaultDigitalOceanSpacesClient(doSpacesExtension, project.logger)
            // Register the version check task
            tasks.register<DigitalOceanSpacesCheckVersionTask>("checkDigitalOceanSpacesVersion") {
                group = "verification"
                description = "Checks if the current version already exists in Digital Ocean Spaces"
                this.extension.set(doSpacesExtension)
                this.s3Client = doSpacesClient.s3Client()
            }

            logger.lifecycle(" | [INFO] Registering `uploadToDigitalOceanSpaces` task")
            if (doSpacesExtension.dryRun) {
                logger.lifecycle(" | [INFO] Dry run mode enabled, uploads will not be performed.")
            }

            // Register the upload task
            tasks.register<DigitalOceanSpacesUploadTask>("uploadToDigitalOceanSpaces") {
                group = "publishing"
                description = "Uploads artifacts to Digital Ocean Spaces"
                this.digitalOceanSpacesClient = if (doSpacesExtension.dryRun) {
                    DryRunDigitalOceanSpacesClient(doSpacesExtension, project.logger)
                } else {
                    doSpacesClient
                }
                jarQualifier = doSpacesExtension.jarQualifier ?: project.name
                checkS3Client = doSpacesClient.s3Client()

                dependsOn("build", "assembleMavenArtifacts")

                // If using the maven-publish plugin, also depend on publish tasks
                plugins.withId("maven-publish") {
                    dependsOn("publishToMavenLocal")
                    dependsOn("generatePomFileForMavenPublication")
                }
            }
        }
    }
}