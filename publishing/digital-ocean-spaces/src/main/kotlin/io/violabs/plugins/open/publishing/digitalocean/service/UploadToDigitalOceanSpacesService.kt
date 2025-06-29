package io.violabs.plugins.open.publishing.digitalocean.service

import io.violabs.plugins.open.publishing.digitalocean.adapter.ProjectAdapter
import io.violabs.plugins.open.publishing.digitalocean.client.DigitalOceanSpacesClient
import io.violabs.plugins.open.publishing.digitalocean.task.DigitalOceanSpacesCheckVersionTask
import org.gradle.api.GradleException
import software.amazon.awssdk.services.s3.S3Client
import java.io.File
import kotlin.sequences.plus

class UploadToDigitalOceanSpacesService(
    private val project: ProjectAdapter,
    private val digitalOceanSpacesClient: DigitalOceanSpacesClient,
    private val checkS3Client: S3Client,
    private val isPlugin: Boolean = false,
    private val checkVersionFunction: (ProjectAdapter, DigitalOceanSpacesClient, S3Client) -> Unit = { p, c, s ->
        DigitalOceanSpacesCheckVersionTask.Companion.checkVersion(p.project, c.ext, s)
    }
) {
    private val buildDir: File
        get() = project.buildDir

    @Throws(GradleException::class)
    fun uploadToSpaces() {
        checkVersionFunction(project, digitalOceanSpacesClient, checkS3Client)

        val filesToUpload: List<File> =
            createJar()
                .plus(createSourcesJar())
                .plus(createJavadocJar())
                .plus(createKdocJar())
                .plus(createPomFile())
                .toList()

        filesToUpload.forEach(digitalOceanSpacesClient::uploadFile)

        if (isPlugin) {
            uploadGeneratedPluginMarkers()
        }
    }

    private fun createJar(): Sequence<File> = buildDir.createLibFiles()
    private fun createSourcesJar(): Sequence<File> = buildDir.createLibFiles("sources")
    private fun createKdocJar(): Sequence<File> = buildDir.createLibFiles("kdoc")
    private fun createJavadocJar(): Sequence<File> = buildDir.createLibFiles("javadoc")


    /**
     * Creates the POM file for the project.
     * This method copies the generated POM file from the publications directory
     * to the libs directory with the proper Maven naming convention.
     *
     * @param buildDir The build directory where the POM file will be created.
     * @return The created POM file with proper naming, or null if the source POM does not exist.
     */
    private fun createPomFile(): File {
        val sourcePom = File(buildDir, "publications/maven/pom-default.xml")

        val targetPom = File(buildDir, "libs/${project.name}-${project.version}.pom")

        // Create libs directory if it doesn't exist
        targetPom.parentFile.mkdirs()

        // Copy the POM file with proper naming
        sourcePom.copyTo(targetPom, overwrite = true)

        return targetPom
    }

    private fun uploadGeneratedPluginMarkers() {
        project.pluginAdapters().forEach { publication ->
            handlePluginMarkerUpload(publication)
        }
    }

    private fun handlePluginMarkerUpload(publication: ProjectAdapter.MavenPublicationAdapter) {
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
        return File(this, "libs/${project.name}-${project.version}$preJar.jar$postJar")
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