package io.violabs.plugins.open.publishing.digitalocean

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

class DigitalOceanSpacesPublishPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Create the extension
        val extension = project.extensions.create<DigitalOceanSpacesExtension>("digitalOceanSpaces")

        // Register the version check task
        project.tasks.register<CheckVersionTask>("checkSpacesVersion") {
            group = "verification"
            description = "Checks if the current version already exists in Digital Ocean Spaces"
            this.extension.set(extension)
        }

        // Register the upload task
        project.tasks.register<DigitalOceanSpacesUploadTask>("uploadToDigitalOceanSpaces") {
            group = "publishing"
            description = "Uploads artifacts to Digital Ocean Spaces"
            this.extension.set(extension)

            // Make sure we run after the build task
            dependsOn("build")

            // If using the maven-publish plugin, also depend on publish tasks
            project.plugins.withId("maven-publish") {
                dependsOn("publishToMavenLocal")
                dependsOn("generatePomFileForMavenPublication")
            }
        }
    }
}