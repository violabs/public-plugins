package io.violabs.plugins.open.publishing.digitalocean.core.adapter

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import java.io.File

class DefaultProjectAdapter(
    override val project: Project,
    override val buildDir: File = project.layout.buildDirectory.get().asFile,
    override val name: String = project.name,
    override val version: String = project.version.toString(),
    override val logger: Logger = project.logger
) : ProjectAdapter {
    override fun pluginAdapters(): List<ProjectAdapter.MavenPublicationAdapter> {
        return project.extensions.getByType<PublishingExtension>()
            .publications
            .withType<MavenPublication>()
            .map { publication ->
                DefaultMavenPublicationAdapter(
                    groupId = publication.groupId ?: project.group.toString(),
                    artifactId = publication.artifactId,
                    version = publication.version ?: project.version.toString(),
                    name = publication.name
                )
            }
    }

    class DefaultMavenPublicationAdapter(
        override val groupId: String,
        override val artifactId: String,
        override val version: String,
        override val name: String
    ) : ProjectAdapter.MavenPublicationAdapter
}