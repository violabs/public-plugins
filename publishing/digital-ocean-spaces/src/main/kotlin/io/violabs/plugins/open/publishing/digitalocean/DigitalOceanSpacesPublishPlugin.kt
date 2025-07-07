package io.violabs.plugins.open.publishing.digitalocean

import io.violabs.plugins.open.publishing.digitalocean.service.DigitalOceanSpacesPublishPluginService
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin to manage publishing jars to digital ocean spaces.
 */
class DigitalOceanSpacesPublishPlugin : Plugin<Project> {
    private val pluginService = DigitalOceanSpacesPublishPluginService()

    /**
     * Applies the extension and registers tasks for Digital Ocean Spaces publishing.
     * @param project The Gradle project to which this plugin is applied.]
     */
    override fun apply(project: Project) = project.run {
        pluginService.apply(project)
    }
}