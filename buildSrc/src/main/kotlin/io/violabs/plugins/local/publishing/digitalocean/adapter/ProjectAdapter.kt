package io.violabs.plugins.local.publishing.digitalocean.adapter

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File

interface ProjectAdapter {
    val project: Project
    val buildDir: File
    val group: String
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