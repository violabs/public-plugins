package io.violabs.plugins.open.publishing.digitalocean.client

import io.violabs.plugins.open.publishing.digitalocean.domain.DigitalOceanSpacesExtension
import org.gradle.api.logging.Logger
import java.io.File

class DryRunDigitalOceanSpacesClient(
    ext: DigitalOceanSpacesExtension,
    logger: Logger
) : DigitalOceanSpacesClient(ext, logger) {
    override fun uploadFile(file: File) {
        if (!file.exists()) return logger.warn("File ${file.name} does not exist, skipping upload")

        val key = "${ext.artifactPath ?: ""}/${file.name}"
        logger.lifecycle("  | Dry run: would upload ${file.name} to ${ext.bucket}/$key")
    }
}