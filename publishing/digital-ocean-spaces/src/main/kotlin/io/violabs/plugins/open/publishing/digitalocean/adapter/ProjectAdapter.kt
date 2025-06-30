package io.violabs.plugins.open.publishing.digitalocean.adapter

import org.gradle.api.Project
import java.io.File

interface ProjectAdapter {
    val project: Project
    val buildDir: File
    val name: String
    val version: String
    fun pluginAdapters(): List<MavenPublicationAdapter>

    interface MavenPublicationAdapter {
        val groupId: String
        val artifactId: String
        val version: String
        val name: String
    }
}