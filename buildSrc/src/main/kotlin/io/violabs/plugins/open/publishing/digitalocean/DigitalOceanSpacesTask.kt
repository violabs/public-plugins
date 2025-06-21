package io.violabs.plugins.open.publishing.digitalocean

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

abstract class DigitalOceanSpacesTask : DefaultTask() {

    /**
     * Creates an S3Client configured for Digital Ocean Spaces.
     * Uses the access key, secret key, endpoint, and region from the provided extension.
     * @param extProp The DigitalOceanSpacesExtension containing configuration information.
     * @return An S3Client instance configured for Digital Ocean Spaces.
     */
    protected fun withS3Client(extProp: Property<DigitalOceanSpacesExtension>, client: S3Client.() -> Unit) {
        val ext = extProp.get()
        requireNotNull(ext.accessKey) { "accessKey is required" }
        requireNotNull(ext.secretKey) { "secretKey is required" }

        val credentials = AwsBasicCredentials.create(ext.accessKey, ext.secretKey)

        val s3 = S3Client.builder()
            .endpointOverride(URI.create(ext.endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(Region.of(ext.region))
            .build()

        s3.use(client)
    }
}