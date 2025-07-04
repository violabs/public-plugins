package io.violabs.plugins.local.publishing.projectsync

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class SyncProjectTask : DefaultTask() {
    @get:Input
    abstract var extension: ProjectSyncExtension

    @TaskAction
    fun sync(): Unit = project.run {
        val source = extension.syncSource ?: layout.projectDirectory.asFile
        val target = extension.syncTarget ?: throw IllegalArgumentException("syncTarget must be specified.")

        val sourceFiles = source
            .walkTopDown()
            .filter { it.isFile }
            .toList()

        logger.lifecycle("Syncing project files from $source to $target")
        sourceFiles.forEach { file -> logger.lifecycle("Copying to buildSrc: ${file.path}") }
    }
}