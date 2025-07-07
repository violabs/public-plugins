package io.violabs.plugins.local.publishing.mavengenerated.task

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType
import java.io.File

abstract class MovePomForPublicationTask : DefaultTask() {
    @get:Input
    abstract val publicationName: Property<String>

    @TaskAction
    fun movePom() {
        val publication = extensions.getByType<PublishingExtension>()
            .publications.getByName(publicationName.get()) as MavenPublication
        handlePluginMarkerUpload(project.layout.buildDirectory.get().asFile, publication, isPlugin = false)
    }


    private fun handlePluginMarkerUpload(buildDir: File, publication: MavenPublication, isPlugin: Boolean = false) {
        val groupId = publication.groupId
        val artifactId = publication.artifactId
        val version = publication.version

        val pomFile = File(buildDir, "publications/${publication.name}/pom-default.xml")
        if (!pomFile.exists()) throw IllegalStateException("POM file does not exist: ${pomFile.absolutePath}")

        val newPom = File(buildDir, "libs/$artifactId-$version.pom")
        newPom.parentFile.mkdirs()
        pomFile.copyTo(newPom, overwrite = true)

        if (!isPlugin) return

        val targetPom = File(
            buildDir,
            "libs/$groupId.$artifactId.gradle.plugin-${publication.version}.pom"
        )
        targetPom.parentFile.mkdirs()
        pomFile.copyTo(targetPom, overwrite = true)
    }

}