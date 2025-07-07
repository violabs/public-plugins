package io.violabs.plugins.open.publishing.digitalocean.domain

import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer

val TaskContainer.uploadToDigitalOceanSpaces: Task?
    get() = this.findByName("uploadToDigitalOceanSpaces")