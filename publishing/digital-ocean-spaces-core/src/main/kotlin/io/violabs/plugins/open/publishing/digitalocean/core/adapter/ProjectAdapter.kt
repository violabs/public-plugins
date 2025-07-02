package io.violabs.plugins.open.publishing.digitalocean.core.adapter

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File

interface ProjectAdapter {
    val project: Project
    val buildDir: File
    val name: String
    val version: String
    val logger: Logger
    fun pluginAdapters(): List<MavenPublicationAdapter>

    interface MavenPublicationAdapter {
        val groupId: String
        val artifactId: String
        val version: String
        val name: String
    }
}