package io.violabs.plugins.local.publishing.digitalocean.client

import io.violabs.plugins.local.publishing.digitalocean.domain.DigitalOceanSpacesExtension
import org.gradle.api.logging.Logger
import java.io.File

abstract class DigitalOceanSpacesClient(
    val ext: DigitalOceanSpacesExtension,
    protected val logger: Logger
) {
    abstract fun uploadFile(file: File)

    val File.isPluginPomType: Boolean
        get() = name.contains("gradle.plugin") && name.contains("pom")

    fun getKey(file: File, artifactPath: String): String {
        return if (file.isPluginPomType) {
            logger.lifecycle("  | ${file.name} is a plugin POM, uploading to plugin path")
            artifactPath.replace("/", ".") + ".gradle.plugin/${ext.publishedVersion}/${file.name}"
        } else "$artifactPath/${file.name}"
    }
}