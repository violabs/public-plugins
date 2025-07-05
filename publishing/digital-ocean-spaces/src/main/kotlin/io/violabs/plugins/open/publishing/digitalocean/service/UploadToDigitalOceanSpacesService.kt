package io.violabs.plugins.open.publishing.digitalocean.service

import io.violabs.plugins.open.publishing.digitalocean.adapter.ProjectAdapter
import io.violabs.plugins.open.publishing.digitalocean.client.DigitalOceanSpacesClient
import org.gradle.api.GradleException
import org.gradle.api.Project
import software.amazon.awssdk.services.s3.S3Client
import java.io.File
import java.security.MessageDigest

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

        val pomFiles = copyPomFiles(
            publicationName = "digitalOceanSpaces",
            groupId = project.group,
            artifactId = project.name,
            version = project.version
        )

        val filesToUpload: List<File> =
            createJar()
                .plus(createSourcesJar())
                .plus(createJavadocJar())
                .plus(createKdocJar())
                .plus(pomFiles)
                .toList()

        filesToUpload.forEach(digitalOceanSpacesClient::uploadFile)
    }

    private fun createJar(): Sequence<File> = buildDir.createLibFiles()
    private fun createSourcesJar(): Sequence<File> = buildDir.createLibFiles("sources")
    private fun createKdocJar(): Sequence<File> = buildDir.createLibFiles("kdoc")
    private fun createJavadocJar(): Sequence<File> = buildDir.createLibFiles("javadoc")

    private fun copyPomFiles(
        publicationName: String,
        groupId: String,
        artifactId: String,
        version: String
    ): Sequence<File> {
        val pomFile = File(buildDir, "publications/$publicationName/pom-default.xml")
        if (!pomFile.exists()) {
            throw IllegalStateException("POM file does not exist: ${pomFile.absolutePath}")
        }

        val fileName = "libs/${artifactId}-${version}.pom"

        val newPom = File(buildDir, fileName)

        newPom.parentFile.mkdirs()

        pomFile.copyTo(newPom, overwrite = true)

        project.project.addGenerateHashesTask()

        val sha1 = File(buildDir, "$fileName.sha1").apply {
            parentFile.mkdirs()
            writeText(
                newPom.generateHash("SHA-1")
            )
        }

        val sha256 = File(buildDir, "$fileName.sha256").apply {
            parentFile.mkdirs()
            writeText(
                newPom.generateHash("SHA-256")
            )
        }

        val pluginPom = pluginPom(newPom, groupId, artifactId, version)

        return sequenceOf(newPom, sha1, sha256) + pluginPom
    }

    private fun pluginPom(
        newPom: File,
        groupId: String,
        artifactId: String,
        version: String
    ): Sequence<File> {
        if (!isPlugin) {
            return emptySequence()
        }

        val fileName = "libs/$groupId.$artifactId.gradle.plugin-${version}.pom"

        val targetPom = File(buildDir, fileName)

        targetPom.parentFile.mkdirs()

        newPom.copyTo(targetPom, overwrite = true)

        project.project.addGenerateHashesTask()

        val sha1 = File(buildDir, "$fileName.sha1").apply {
            parentFile.mkdirs()
            writeText(
                targetPom.generateHash("SHA-1")
            )
        }

        val sha256 = File(buildDir, "$fileName.sha256").apply {
            parentFile.mkdirs()
            writeText(
                targetPom.generateHash("SHA-256")
            )
        }

        return sequenceOf(targetPom, sha1, sha256)
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


    fun Project.addGenerateHashesTask() {
        val libsDir = file("${layout.buildDirectory.get()}/libs")

        val files = libsDir.listFiles()?.filter { it.isFile }

        val fileNames = files?.map { it.name } ?: emptyList()

        val baseFiles = files?.filter { it.extension in listOf("jar", "war", "aar", "pom") } ?: emptyList()

        val filesToHash = baseFiles.filter {
            val hashed1 = "$it.sha1"
            val hashed2 = "$it.sha256"
            hashed1 !in fileNames && hashed2 !in fileNames
        }

        filesToHash
            .onEach { println("  | Generating hashes for file: ${it.name}") }
            .forEach { file ->
                if (file.isFile) {
                    listOf("SHA-256", "SHA-1").forEach { algo ->
                        val hash = file.generateHash(algo)
                        val ext = when (algo) {
                            "SHA-1" -> "sha1"
                            "SHA-256" -> "sha256"
                            else -> algo.lowercase()
                        }
                        file.resolveSibling("${file.name}.$ext").writeText(hash)
                        logger.lifecycle("  | Created file: ${file.name}.$ext")
                    }
                }
            }

    }

    fun File.generateHash(hashAlgo: String): String {
        val buffer = ByteArray(1024 * 4)
        val md = MessageDigest.getInstance(hashAlgo)
        inputStream().use { fis ->
            var bytes = fis.read(buffer)
            while (bytes >= 0) {
                if (bytes > 0) md.update(buffer, 0, bytes)
                bytes = fis.read(buffer)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}