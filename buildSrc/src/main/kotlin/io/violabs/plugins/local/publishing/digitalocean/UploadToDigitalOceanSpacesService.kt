package io.violabs.plugins.local.publishing.digitalocean

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import software.amazon.awssdk.services.s3.S3Client
import java.io.File
import kotlin.collections.forEach

class UploadToDigitalOceanSpacesService(
    private val project: Project,
    private val digitalOceanSpacesClient: DigitalOceanSpacesClient,
    private val checkS3Client: S3Client,
    private val jarQualifier: String? = null,
    private val isPlugin: Boolean = false
) {
    fun uploadToSpaces() {
        try {
            DigitalOceanSpacesCheckVersionTask.checkVersion(
                project,
                digitalOceanSpacesClient.ext,
                checkS3Client
            )
        } catch (e: Exception) {
            project.logger.warn("Version check failed, but continuing due to configuration: ${e.message}")
            return
        }

        val buildDir: File = project.layout.buildDirectory.get().asFile

        val filesToUpload: List<File> =
            createJar(buildDir)
                .plus(createSourcesJar(buildDir))
                .plus(createJavadocJar(buildDir))
                .plus(createKdocJar(buildDir))
                .plus(createPomFile(buildDir))
                .toList()

        filesToUpload.forEach(digitalOceanSpacesClient::uploadFile)

        if (isPlugin) {
            uploadGeneratedPluginMarkers(buildDir)
        }
    }

    private fun createJar(buildDir: File): Sequence<File> = buildDir.createLibFiles()
    private fun createSourcesJar(buildDir: File): Sequence<File> = buildDir.createLibFiles("sources")
    private fun createKdocJar(buildDir: File): Sequence<File> = buildDir.createLibFiles("kdoc")
    private fun createJavadocJar(buildDir: File): Sequence<File> = buildDir.createLibFiles("javadoc")


    /**
     * Creates the POM file for the project.
     * This method copies the generated POM file from the publications directory
     * to the libs directory with the proper Maven naming convention.
     *
     * @param buildDir The build directory where the POM file will be created.
     * @return The created POM file with proper naming, or null if the source POM does not exist.
     */
    private fun createPomFile(buildDir: File): File {
        val sourcePom = File(buildDir, "publications/maven/pom-default.xml")

        val targetPom = File(buildDir, "libs/${jarQualifier ?: project.name}-${project.version}.pom")

        // Create libs directory if it doesn't exist
        targetPom.parentFile.mkdirs()

        // Copy the POM file with proper naming
        sourcePom.copyTo(targetPom, overwrite = true)

        return targetPom
    }

    private fun uploadGeneratedPluginMarkers(buildDir: File) {
        val publishing = project.extensions.getByType<PublishingExtension>()

        publishing.publications
            .withType<MavenPublication>()
            .filter { it.name.contains("plugin", ignoreCase = true) }
            .forEach { handlePluginMarkerUpload(buildDir, it) }
    }

    private fun handlePluginMarkerUpload(buildDir: File, publication: MavenPublication) {
        val groupId = publication.groupId
        val artifactId = publication.artifactId
        val version = publication.version

        // Get the generated artifacts
        val pomFile = File(buildDir, "publications/${publication.name}/pom-default.xml")

        val jarFile = File(buildDir, "libs/$artifactId-${publication.version}.jar")

        if (!pomFile.exists()) throw IllegalStateException("POM file does not exist: ${pomFile.absolutePath}")

        val targetPom = File(
            buildDir,
            "libs/$groupId.$artifactId.gradle.plugin-${publication.version}.pom"
        )

        // Create libs directory if it doesn't exist
        targetPom.parentFile.mkdirs()

        // Copy the POM file with proper naming
        pomFile.copyTo(targetPom, overwrite = true)
        // Upload with the plugin marker path
        val originalPath = digitalOceanSpacesClient.ext.artifactPath
        val groupPackage = groupId.replace('.', '/')
        digitalOceanSpacesClient.ext.artifactPath = "plugins/$groupPackage/$artifactId/$version"

        digitalOceanSpacesClient.uploadFile(targetPom)
        if (jarFile.exists()) {
            digitalOceanSpacesClient.uploadFile(jarFile)
        }

        digitalOceanSpacesClient.ext.artifactPath = originalPath
    }


    private fun File.createLibFile(preJar: String? = null, postJar: String = ""): File {
        val preJar = preJar?.let { "-$it" } ?: ""
        return File(this, "libs/${jarQualifier ?: project.name}-${project.version}$preJar.jar$postJar")
    }

    private fun File.createLibFiles(preJar: String? = null): Sequence<File> {
        val preJar = preJar?.let { "-$it" } ?: ""
        return sequenceOf(
            createLibFile(preJar),
            createLibFile(preJar, "sha1"),
            createLibFile(preJar, "sha256")
        )
    }
}