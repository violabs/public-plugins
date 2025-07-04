package io.violabs.plugins.local.jar.explorer

import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class JarExplorerPlugin : Plugin<Project> {

    /**
     * Applies the JarExplorerPlugin to the given project.
     * This method is called when the plugin is applied to a project.
     *
     * @param project The Gradle project to which this plugin is applied.
     */
    override fun apply(project: Project) {
        // register JarExploderTask
        project.tasks.register("jarExploder", JarExploderTask::class.java) {
            group = "explorer"
            description = "Explodes the jar file into a directory structure for easier exploration."
        }
    }
}