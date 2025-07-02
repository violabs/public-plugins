package io.violabs.plugins.open.publishing.digitalocean.core.client

import io.violabs.plugins.open.publishing.digitalocean.core.domain.DigitalOceanSpacesExtension
import org.gradle.api.logging.Logger
import java.io.File

abstract class DigitalOceanSpacesClient(
    val ext: DigitalOceanSpacesExtension,
    protected val logger: Logger
) {
    abstract fun uploadFile(file: File)
}