package io.violabs.plugins.open.publishing.digitalocean.core.adapter

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

class DefaultS3BuilderAdapter(
    override val accessKey: String,
    override val secretKey: String,
    override val endpoint: String,
    override val region: String,
) : S3BuilderAdapter {
    override fun build(): S3Client {
        val credentials = AwsBasicCredentials.create(accessKey, secretKey)

        return S3Client.builder()
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(Region.of(region))
            .build()
    }
}