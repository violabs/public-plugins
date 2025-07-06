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

        var filesToUpload: Sequence<File> =
            createJar()
                .plus(createSourcesJar())
                .plus(createJavadocJar())
                .plus(createKdocJar())
                .plus(pomFiles)

        if (isPlugin) {
            val mainJar = File(buildDir, "libs/${project.name}-${project.version}.jar")
            println("${mainJar.exists()}")
            val pluginPath = "libs/${project.name}-${project.version}.gradle.plugin.jar"
            val pluginJar = File(buildDir, pluginPath)
            pluginJar.parentFile.mkdirs()
            mainJar.copyTo(pluginJar, overwrite = true)

            val sha1 = File(buildDir, "$pluginPath.sha1").apply {
                parentFile.mkdirs()
                writeText(pluginJar.generateHash("SHA-1"))
            }

            val sha256 = File(buildDir, "$pluginPath.sha256").apply {
                parentFile.mkdirs()
                writeText(pluginJar.generateHash("SHA-256"))
            }

            filesToUpload = filesToUpload.plus(sequenceOf(pluginJar, sha1, sha256))
        }

        filesToUpload
            .onEach { file -> println("  | found: ${file.name}") }
            .forEach(digitalOceanSpacesClient::uploadFile)
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

        // build a real Maven‐style path: groupId/artifactId/version
        val repoPath = groupId.replace('.', '/') + "/" + artifactId + "/" + version
        val pomName = "$artifactId-$version.pom"
        val newPom = File(buildDir, "$repoPath/$pomName")

        newPom.parentFile.mkdirs()
        pomFile.copyTo(newPom, overwrite = true)

//        project.project.addGenerateHashesTask()

        val sha1 = File(buildDir, "$repoPath/$pomName.sha1").apply {
            parentFile.mkdirs()
            writeText(newPom.generateHash("SHA-1"))
        }

        val sha256 = File(buildDir, "$repoPath/$pomName.sha256").apply {
            parentFile.mkdirs()
            writeText(newPom.generateHash("SHA-256"))
        }

        val pluginFiles = pluginPom(newPom, groupId, artifactId, version)
        return sequenceOf(newPom, sha1, sha256) + pluginFiles
    }

    private fun pluginPom(
        newPom: File,
        groupId: String,
        artifactId: String,
        version: String
    ): Sequence<File> {
        if (!isPlugin) return emptySequence()

        // marker‐pom under plugin‐id path: groupId.artifactId → groupId/artifactId
        val pluginId = "$groupId.$artifactId"
        val pluginPath = pluginId.replace('.', '/')
        val pomName = "$pluginId.gradle.plugin-$version.pom"
        val targetPom = File(buildDir, "$pluginPath/$version/$pomName")

        targetPom.parentFile.mkdirs()
        newPom.copyTo(targetPom, overwrite = true)
        project.project.addGenerateHashesTask()

        val sha1 = File(buildDir, "$pluginPath/$version/$pomName.sha1").apply {
            parentFile.mkdirs()
            writeText(targetPom.generateHash("SHA-1"))
        }

        val sha256 = File(buildDir, "$pluginPath/$version/$pomName.sha256").apply {
            parentFile.mkdirs()
            writeText(targetPom.generateHash("SHA-256"))
        }

        return sequenceOf(targetPom, sha1, sha256)
    }

    private fun File.createLibFile(preJar: String? = null, postJar: String = "", fileType: String = "jar"): File {
        val pre = preJar?.let { "-$it" } ?: ""
        val post = if (postJar.isEmpty()) "" else ".$postJar"
        return File(this, "libs/${project.name}-${project.version}$pre.$fileType$post")
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
        val files = libsDir.listFiles()?.filter { it.isFile } ?: return

        // Skip any file that already *is* a hash
        val candidates = files.filter { !it.name.endsWith(".sha1") && !it.name.endsWith(".sha256") }

        val existing = files.map { it.name }.toSet()
        val toHash = candidates.filter { f ->
            "${f.name}.sha1" !in existing && "${f.name}.sha256" !in existing
        }

        toHash.onEach { println("  | Generating hashes for file: ${it.name}") }
            .forEach { f ->
                listOf("SHA-1" to "sha1", "SHA-256" to "sha256").forEach { (algo, ext) ->
                    f.resolveSibling("${f.name}.$ext").writeText(f.generateHash(algo))
                    logger.lifecycle("  | Created file: ${f.name}.${ext}")
                }
            }
    }

    fun File.generateHash(hashAlgo: String): String {
        val buffer = ByteArray(4 * 1024)
        val md = MessageDigest.getInstance(hashAlgo)
        inputStream().use { fis ->
            var read = fis.read(buffer)
            while (read >= 0) {
                if (read > 0) md.update(buffer, 0, read)
                read = fis.read(buffer)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
