package io.violabs.plugins.local.publishing.digitalocean.service

import io.violabs.plugins.local.publishing.digitalocean.adapter.ProjectAdapter
import io.violabs.plugins.local.publishing.digitalocean.client.DigitalOceanSpacesClient
import org.gradle.api.GradleException
import software.amazon.awssdk.services.s3.S3Client
import java.io.File
import kotlin.sequences.plus

class UploadToDigitalOceanSpacesService(
    private val project: ProjectAdapter,
    private val digitalOceanSpacesClient: DigitalOceanSpacesClient,
    private val checkS3Client: S3Client,
    private val isPlugin: Boolean = false,
    private val checkVersionService: CheckVersionDigitalOceanSpacesService
) {
    private val buildDir: File
        get() = project.buildDir

    @Throws(GradleException::class)
    fun uploadToSpaces() {
        checkVersionService.checkVersion(project, digitalOceanSpacesClient.ext, checkS3Client)

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
    private fun createPomFile(): Sequence<File> = buildDir.createLibFiles(fileType = "pom")

    private fun uploadGeneratedPluginMarkers() {
        project.pluginAdapters().forEach { publication ->
            handlePluginMarkerUpload(publication)
        }
    }

    private fun handlePluginMarkerUpload(publication: ProjectAdapter.MavenPublicationAdapter) {
        val groupId = publication.groupId
        val artifactId = publication.artifactId
        val version = publication.version

        println("[DEBUG] Handling plugin marker ${publication.name}")
        val pomLocation = File(buildDir, "publications/${publication.name}")
        println("[DEBUG] POM location: ${pomLocation.absolutePath}")
        println("[DEBUG] exists: ${pomLocation.exists()}")
        // Get the generated artifacts
        val pomFile = File(buildDir, "publications/${publication.name}/pom-default.xml")

        val jarFile = File(buildDir, "libs/$artifactId-${publication.version}.jar")

        if (!pomFile.exists()) throw IllegalStateException("POM file does not exist: ${pomFile.absolutePath}")

        println("[DEBUG] Uploading plugin marker for $groupId:$artifactId:$version")

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

    private fun File.createLibFile(preJar: String? = null, postJar: String = "", fileType: String = "jar"): File {
        val preJarSetup = preJar?.let { "-$it" } ?: ""
        val postJarSetup = if (postJar.isEmpty()) "" else ".$postJar"
        return File(this, "libs/${project.name}-${project.version}$preJarSetup.$fileType$postJarSetup")
    }

    private fun File.createLibFiles(preJar: String? = null, fileType: String = "jar"): Sequence<File> {
        return sequenceOf(
            createLibFile(preJar, fileType = fileType),
            createLibFile(preJar, "sha1", fileType),
            createLibFile(preJar, "sha256", fileType)
        )
    }
}