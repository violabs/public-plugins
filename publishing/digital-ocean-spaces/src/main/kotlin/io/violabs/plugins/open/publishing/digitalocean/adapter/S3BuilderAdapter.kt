package io.violabs.plugins.open.publishing.digitalocean.adapter

import software.amazon.awssdk.services.s3.S3Client

interface S3BuilderAdapter {
    val accessKey: String
    val secretKey: String
    val endpoint: String
    val region: String

    fun build(): S3Client
}