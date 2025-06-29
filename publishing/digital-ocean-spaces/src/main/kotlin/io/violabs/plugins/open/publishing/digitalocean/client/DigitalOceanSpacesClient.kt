package io.violabs.plugins.open.publishing.digitalocean.client

import io.violabs.plugins.open.publishing.digitalocean.domain.DigitalOceanSpacesExtension
import io.violabs.plugins.open.publishing.digitalocean.adapter.DefaultS3BuilderAdapter
import io.violabs.plugins.open.publishing.digitalocean.adapter.S3BuilderAdapter
import org.gradle.api.logging.Logger
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File

abstract class DigitalOceanSpacesClient(
    val ext: DigitalOceanSpacesExtension,
    protected val logger: Logger
) {
    abstract fun uploadFile(file: File)
}