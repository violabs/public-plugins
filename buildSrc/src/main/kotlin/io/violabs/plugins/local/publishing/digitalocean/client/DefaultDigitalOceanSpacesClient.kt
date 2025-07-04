package io.violabs.plugins.local.publishing.digitalocean.client

import io.violabs.plugins.local.publishing.digitalocean.adapter.DefaultS3BuilderAdapter
import io.violabs.plugins.local.publishing.digitalocean.adapter.S3BuilderAdapter
import io.violabs.plugins.local.publishing.digitalocean.domain.DigitalOceanSpacesExtension
import org.gradle.api.logging.Logger
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File

class DefaultDigitalOceanSpacesClient(
    ext: DigitalOceanSpacesExtension,
    logger: Logger,
    val s3ClientBuilder: (accessKey: String, secretKey: String) -> S3BuilderAdapter = { accessKey, secretKey ->
        DefaultS3BuilderAdapter(
            accessKey = accessKey,
            secretKey = secretKey,
            endpoint = ext.endpoint,
            region = ext.region
        )
    }
) : DigitalOceanSpacesClient(ext, logger) {
    fun s3Client(): S3Client {
        val accessKey = requireNotNull(ext.accessKey) { "accessKey is required" }
        val secretKey = requireNotNull(ext.secretKey) { "secretKey is required" }

        return s3ClientBuilder(accessKey, secretKey).build()
    }

    /**
     * Uploads a file to Digital Ocean Spaces.
     * If the file does not exist, it logs a warning and skips the upload.
     *
     * @param file The file to upload.
     */
    override fun uploadFile(file: File) {
        val bucket = requireNotNull(ext.bucket) { "bucket is required" }
        val artifactPath = requireNotNull(ext.artifactPath) { "artifactPath is required" }

        val client = s3Client()
        try {
            client.use {
                if (!file.exists()) return@use logger.warn("File ${file.name} does not exist, skipping upload")

                val key = "$artifactPath/${file.name}"

                logger.lifecycle("Uploading ${file.name} to ${bucket}/$key")

                val request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build()

                it.putObject(request, file.toPath())
            }
        } catch (e: Exception) {
            logger.error("Failed to upload file ${file.name} to Digital Ocean Spaces", e)
            client.close()
        }
    }
}