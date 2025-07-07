package io.violabs.plugins.local.publishing.digitalocean.client

import io.violabs.plugins.local.publishing.digitalocean.domain.DigitalOceanFile
import io.violabs.plugins.local.publishing.digitalocean.domain.DigitalOceanSpacesExtension
import org.gradle.api.logging.Logger

abstract class DigitalOceanSpacesClient(
    val ext: DigitalOceanSpacesExtension,
    protected val logger: Logger
) {
    abstract fun uploadFile(doFile: DigitalOceanFile)
}