package io.violabs.plugins.local.publishing.digitalocean.service

import io.violabs.plugins.local.publishing.digitalocean.adapter.ProjectAdapter
import io.violabs.plugins.local.publishing.digitalocean.client.DigitalOceanSpacesClient
import io.violabs.plugins.local.publishing.digitalocean.domain.DigitalOceanFile
import io.violabs.plugins.local.publishing.digitalocean.domain.Namespace
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

        val namespace = Namespace(
            groupId = project.group,
            artifactId = project.name,
            version = project.version
        )

        val pomFiles = copyPomFiles(namespace)

        var filesToUpload: Sequence<DigitalOceanFile> =
            createJar(namespace)
                .plus(createSourcesJar(namespace))
                .plus(createJavadocJar(namespace))
                .plus(createKdocJar(namespace))
                .plus(pomFiles)

        if (isPlugin) {
            val mainJar = File(buildDir, namespace.jarPath)
            val pluginJar = File(buildDir, namespace.pluginJarPath)
            pluginJar.parentFile.mkdirs()
            mainJar.copyTo(pluginJar, overwrite = true)

            val pluginJarDoFile = DigitalOceanFile(
                namespace.pluginJarKey,
                pluginJar
            )

            val sha1 = DigitalOceanFile(
                namespace.pluginJarSha1Key,
                File(buildDir, namespace.pluginJarSha1Path).apply {
                    parentFile.mkdirs()
                    writeText(pluginJar.generateHash("SHA-1"))
                }
            )

            val sha256 = DigitalOceanFile(
                namespace.pluginJarSha256Key,
                File(buildDir, namespace.pluginJarSha256Path).apply {
                    parentFile.mkdirs()
                    writeText(pluginJar.generateHash("SHA-256"))
                }
            )

            filesToUpload = filesToUpload.plus(sequenceOf(pluginJarDoFile, sha1, sha256))
        }

        filesToUpload
            .forEach(digitalOceanSpacesClient::uploadFile)
    }

    private fun createJar(namespace: Namespace): Sequence<DigitalOceanFile> = sequenceOf(
        DigitalOceanFile(namespace.jarKey, File(buildDir, namespace.jarPath)),
        DigitalOceanFile(namespace.jarSha1Key, File(buildDir, namespace.jarSha1Path)),
        DigitalOceanFile(namespace.jarSha256Key, File(buildDir, namespace.jarSha256Path))
    )

    private fun createSourcesJar(namespace: Namespace): Sequence<DigitalOceanFile> = sequenceOf(
        DigitalOceanFile(namespace.sourcesJarKey, File(buildDir, namespace.sourcesJarPath)),
        DigitalOceanFile(namespace.sourcesJarSha1Key, File(buildDir, namespace.sourcesJarSha1Path)),
        DigitalOceanFile(namespace.sourcesJarSha256Key, File(buildDir, namespace.sourcesJarSha256Path))
    )

    private fun createKdocJar(namespace: Namespace): Sequence<DigitalOceanFile> = sequenceOf(
        DigitalOceanFile(namespace.kdocJarKey, File(buildDir, namespace.kdocJarPath)),
        DigitalOceanFile(namespace.kdocJarSha1Key, File(buildDir, namespace.kdocJarSha1Path)),
        DigitalOceanFile(namespace.kdocJarSha256Key, File(buildDir, namespace.kdocJarSha256Path))
    )

    private fun createJavadocJar(namespace: Namespace): Sequence<DigitalOceanFile> = sequenceOf(
        DigitalOceanFile(namespace.javadocJarKey, File(buildDir, namespace.javadocJarPath)),
        DigitalOceanFile(namespace.javadocJarSha1Key, File(buildDir, namespace.javadocJarSha1Path)),
        DigitalOceanFile(namespace.javadocJarSha256Key, File(buildDir, namespace.javadocJarSha256Path))
    )

    private fun copyPomFiles(namespace: Namespace): Sequence<DigitalOceanFile> {
        val pomFile = File(buildDir, namespace.sourcePomName)
        if (!pomFile.exists()) {
            throw IllegalStateException("POM file does not exist: ${pomFile.absolutePath}")
        }

        val newPom = File(buildDir, namespace.pomPath)

        newPom.parentFile.mkdirs()
        pomFile.copyTo(newPom, overwrite = true)

        val finalPom = DigitalOceanFile(
            namespace.pomKey,
            newPom
        )

//        project.project.addGenerateHashesTask()

        val sha1 = DigitalOceanFile(
            namespace.pomSha1Key,
            File(buildDir, namespace.pomSha1Path).apply {
                parentFile.mkdirs()
                writeText(newPom.generateHash("SHA-1"))
            }
        )

        val sha256 = DigitalOceanFile(
            namespace.pomSha256Key,
            File(buildDir, namespace.pomSha256Path).apply {
                parentFile.mkdirs()
                writeText(newPom.generateHash("SHA-256"))
            })

        val pluginFiles = pluginPom(namespace)
        return sequenceOf(finalPom, sha1, sha256) + pluginFiles
    }

    private fun pluginPom(namespace: Namespace): Sequence<DigitalOceanFile> {
        if (!isPlugin) return emptySequence()

        val targetPom = File(buildDir, namespace.pluginPomPath)
        targetPom.parentFile.mkdirs()

        val pluginMarkerPomContent = """
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
              <modelVersion>4.0.0</modelVersion>
              <groupId>${namespace.pluginMarkerGroup}</groupId>
              <artifactId>${namespace.pluginMarkerArtifact}</artifactId>
              <version>${namespace.version}</version>
              <packaging>pom</packaging>
              <name>Gradle Plugin Marker for ${namespace.pluginMarkerGroup}</name>
              <description>Marker for Gradle plugin '${namespace.pluginMarkerGroup}'</description>
              <dependencies>
                <dependency>
                  <groupId>${namespace.groupId}</groupId>
                  <artifactId>${namespace.artifactId}</artifactId>
                  <version>${namespace.version}</version>
                  <scope>runtime</scope>
                </dependency>
              </dependencies>
            </project>
        """.trimIndent()

        // --- Write marker POM to targetPom ---
        targetPom.writeText(pluginMarkerPomContent)

        project.project.addGenerateHashesTask()

        val finalPom = DigitalOceanFile(namespace.pluginPomKey, targetPom)
        val sha1 = DigitalOceanFile(
            namespace.pluginPomSha1Key,
            File(buildDir, namespace.pluginPomSha1Path).apply {
                parentFile.mkdirs()
                writeText(targetPom.generateHash("SHA-1"))
            }
        )
        val sha256 = DigitalOceanFile(
            namespace.pluginPomSha256Key,
            File(buildDir, namespace.pluginPomSha256Path).apply {
                parentFile.mkdirs()
                writeText(targetPom.generateHash("SHA-256"))
            }
        )

        return sequenceOf(finalPom, sha1, sha256)
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
