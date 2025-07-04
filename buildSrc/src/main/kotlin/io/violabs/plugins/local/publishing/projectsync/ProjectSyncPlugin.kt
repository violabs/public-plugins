package io.violabs.plugins.local.publishing.projectsync

import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class ProjectSyncPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("projectSync", ProjectSyncExtension::class.java)

        project.tasks.register("projectSync", ProjectSyncTask::class.java) {
            group = "sync"
            description = "Synchronizes project files from source to target directory."
            this.extension = extension
        }

        project.tasks.named("build") {
            dependsOn("projectSync")
        }
    }
}