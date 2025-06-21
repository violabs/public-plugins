package io.violabs.plugins.open.publishing.digitalocean

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.net.URI
import kotlin.collections.plusAssign

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