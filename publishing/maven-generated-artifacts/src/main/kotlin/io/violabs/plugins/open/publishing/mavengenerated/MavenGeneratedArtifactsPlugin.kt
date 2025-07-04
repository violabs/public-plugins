package io.violabs.plugins.open.publishing.mavengenerated

import io.violabs.plugins.open.publishing.mavengenerated.service.MavenGeneratedArtifactsPublishPluginService
import org.gradle.api.Plugin
import org.gradle.api.Project

class MavenGeneratedArtifactsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val service = MavenGeneratedArtifactsPublishPluginService()

        service.apply(project)
    }
}