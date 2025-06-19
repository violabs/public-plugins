package io.violabs.plugins.open.publishing.digitalocean

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.net.URI
import kotlin.collections.forEach
import kotlin.collections.plusAssign
import kotlin.io.appendText
import kotlin.jvm.java
import kotlin.text.trimIndent
import kotlin.text.trimMargin
import kotlin.use

open class DigitalOceanSpacesExtension {
    var accessKey: String? = null
    var secretKey: String? = null
    var region: String = "nyc3"  // Default region
    var bucket: String? = null
    var endpoint: String = "https://nyc3.digitaloceanspaces.com"  // Default endpoint
    var artifactPath: String? = null  // Path within the bucket
}

open class DigitalOceanSpacesUploadTask : DefaultTask() {
    @get:Input
    val extension: Property<DigitalOceanSpacesExtension> = project.objects.property(DigitalOceanSpacesExtension::class.java)

    @TaskAction
    fun uploadToSpaces() {
        val ext = extension.get()

        // Validate required properties
        requireNotNull(ext.accessKey) { "accessKey is required" }
        requireNotNull(ext.secretKey) { "secretKey is required" }
        requireNotNull(ext.bucket) { "bucket is required" }

        val credentials = AwsBasicCredentials.create(ext.accessKey, ext.secretKey)

        val s3Client = S3Client.builder()
            .endpointOverride(URI.create(ext.endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(Region.of(ext.region))
            .build()

        // Get the build directory
        val buildDir = project.layout.buildDirectory.get().asFile

        // Files to upload
        val filesToUpload = mutableListOf<File>()

        // Add main JAR
        filesToUpload += File(buildDir, "libs/${project.name}-${project.version}.jar")

        // Add sources JAR if it exists
        val sourcesJar = File(buildDir, "libs/${project.name}-${project.version}-sources.jar")
        if (sourcesJar.exists()) filesToUpload += sourcesJar

        // Add JavaDoc JAR if it exists
        val javadocJar = File(buildDir, "libs/${project.name}-${project.version}-javadoc.jar")
        if (javadocJar.exists()) filesToUpload += javadocJar

        // Add POM file
        val pomFile = File(buildDir, "publications/maven/pom-default.xml")
        if (pomFile.exists()) filesToUpload += pomFile

        // Upload each file
        filesToUpload.forEach { file ->
            if (file.exists()) {
                val key = "${ext.artifactPath ?: ""}/${file.name}"

                logger.lifecycle("Uploading ${file.name} to ${ext.bucket}/$key")

                val request = PutObjectRequest.builder()
                    .bucket(ext.bucket)
                    .key(key)
                    .build()

                s3Client.putObject(request, file.toPath())
            } else {
                logger.warn("File ${file.name} does not exist, skipping upload")
            }
        }

        s3Client.close()
    }
}

open class CheckVersionTask : DefaultTask() {
    @get:Input
    val extension: Property<DigitalOceanSpacesExtension> = project.objects.property(DigitalOceanSpacesExtension::class.java)

    @TaskAction
    fun checkVersion() {
        val ext = extension.get()

        requireNotNull(ext.accessKey) { "accessKey is required" }
        requireNotNull(ext.secretKey) { "secretKey is required" }
        requireNotNull(ext.bucket) { "bucket is required" }

        val githubOutput = System.getenv("GITHUB_OUTPUT")
        if (githubOutput != null) {
            File(githubOutput).appendText("""
                version=${project.version}
                name=${project.name}
                tag=${project.name}-${project.version}
                """.trimIndent() + "\n")
        }

        val credentials = AwsBasicCredentials.create(ext.accessKey, ext.secretKey)

        val s3Client = S3Client.builder()
            .endpointOverride(URI.create(ext.endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(Region.of(ext.region))
            .build()

        s3Client.use { client ->
            val key = "${ext.artifactPath ?: ""}/${project.name}-${project.version}.jar"

            val request = HeadObjectRequest.builder()
                .bucket(ext.bucket)
                .key(key)
                .build()

            try {
                client.headObject(request)
                // Version exists - throw error
                val errorMessage = """
                    |::error::Version ${project.version} already exists in Digital Ocean Spaces
                    |Artifact: ${project.name}
                    |Path: ${ext.bucket}/$key
                    |Tag: ${project.name}-${project.version}
                    |Please update the version number in your build.gradle.kts file.
                """.trimMargin()

                throw GradleException(errorMessage)
            } catch (e: NoSuchKeyException) {
                // Version doesn't exist
                logger.lifecycle("""
                    |::notice::Version check passed
                    |Version: ${project.version}
                    |Artifact: ${project.name}
                    |Tag: ${project.name}-${project.version}
                """.trimMargin())
            }
        }
    }
}

class DigitalOceanSpacesPublishPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Create the extension
        val extension = project.extensions.create<DigitalOceanSpacesExtension>("digitalOceanSpaces")

        // Register the version check task
        project.tasks.register<CheckVersionTask>("checkSpacesVersion") {
            group = "verification"
            description = "Checks if the current version already exists in Digital Ocean Spaces"
            this.extension.set(extension)
        }

        // Register the upload task
        project.tasks.register<DigitalOceanSpacesUploadTask>("uploadToDigitalOceanSpaces") {
            group = "publishing"
            description = "Uploads artifacts to Digital Ocean Spaces"
            this.extension.set(extension)

            // Make sure we run after the build task
            dependsOn("build")

            // If using the maven-publish plugin, also depend on publish tasks
            project.plugins.withId("maven-publish") {
                dependsOn("publishToMavenLocal")
                dependsOn("generatePomFileForMavenPublication")
            }
        }
    }
}