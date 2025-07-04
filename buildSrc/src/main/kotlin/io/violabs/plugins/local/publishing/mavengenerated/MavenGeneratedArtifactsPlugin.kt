package io.violabs.plugins.local.publishing.mavengenerated

import io.violabs.plugins.local.publishing.mavengenerated.service.MavenGeneratedArtifactsPublishPluginService
import org.gradle.api.Plugin
import org.gradle.api.Project

class MavenGeneratedArtifactsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val service = MavenGeneratedArtifactsPublishPluginService()

        service.apply(project)
    }
}