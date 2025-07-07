package io.violabs.plugins.open.publishing.mavengenerated.domain

import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer

val TaskContainer.mavenGeneratedArtifacts: Task?
    get() = this.findByName("assembleMavenArtifacts")