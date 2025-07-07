package io.violabs.plugins.local.publishing.digitalocean.client

import io.violabs.plugins.local.publishing.digitalocean.domain.DigitalOceanFile
import io.violabs.plugins.local.publishing.digitalocean.domain.DigitalOceanSpacesExtension
import org.gradle.api.logging.Logger

class DryRunDigitalOceanSpacesClient(
    ext: DigitalOceanSpacesExtension,
    logger: Logger
) : DigitalOceanSpacesClient(ext, logger) {
    override fun uploadFile(doFile: DigitalOceanFile) {
        val file = doFile.file
        if (!file.exists()) return logger.warn("File ${file.name} does not exist, skipping upload")
        logger.lifecycle("  | Dry run: would upload ${file.name} to ${ext.bucket}/${doFile.key}")
    }
}