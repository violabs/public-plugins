package io.violabs.plugins.open.secrets

import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import java.io.File
import java.util.*
import kotlin.apply
import kotlin.collections.count
import kotlin.collections.onEach
import kotlin.io.reader
import kotlin.io.use
import kotlin.jvm.java

/**
 * A plugin that loads secrets from a file or system properties into the project's extra properties.
 */
class SecretsLoaderPlugin : DefaultOutputPlugin() {
    /**
     * Applies the plugin to the given project.
     * It creates an extension for configuring the secrets loader and registers a task to check if secrets exist.
     * @param project The Gradle project to apply the plugin to.
     */
    override fun apply(project: Project) {
        val extension = project.extensions.create("secretsLoader", SecretsLoaderExtension::class.java)

        val amountProcessed: Int = project.rootProject.processSecretsFromFile(
            extension.secretFile ?: "secret.properties",
            extension.systemProperties()
        )

        project.tasks.register("checkSecretsExist", CheckSecretsExistTask::class.java) {
            this.group = "verification"
            this.description = "Check if secrets exist in the secret file"
            this.secretFilePath = project.rootProject.file(extension.secretFile ?: "secret.properties")
            this.amountFound = amountProcessed

            defaultOutputFileDetails(project, this, CheckSecretsExistTask::class)
        }
    }

    /**
     * Load secrets from a file or system properties into the project's extra properties.
     * If the file exists, it loads properties from the file.
     * If the file does not exist, it loads system properties into the extra properties.
     * @receiver Project The Gradle project to process secrets for.
     * @param secretPropertiesName The name of the properties file to load secrets from.
     * @param systemProperties A map of keys to system property names to load into extra properties.
     * @return The number of properties loaded into the extra properties.
     */
    fun Project.processSecretsFromFile(
        secretPropertiesName: String = "secret.properties",
        systemProperties: Map<Ext.Key, Ext.SysPropName> = emptyMap()
    ): Int {
        val secretPropsFile = this.rootProject.file(secretPropertiesName)
        val ext = this.extensions.extraProperties
        return if (secretPropsFile.exists()) {
            processSecretsFromFile(secretPropsFile, ext)
        } else {
            processSystemProps(systemProperties, ext)
        }
    }

    /**
     * Load secrets from a file into the extra properties.
     * @param secretPropsFile The file containing the secrets.
     * @param ext The ExtraPropertiesExtension to load the secrets into.
     * @return The number of properties loaded into the extra properties.
     */
    private fun processSecretsFromFile(secretPropsFile: File, ext: ExtraPropertiesExtension): Int =
        secretPropsFile
            .reader()
            .use { Properties().apply { load(it) } }
            .onEach { (name, value) -> ext[name.toString()] = value }
            .count()

    /**
     * Load system properties into the extra properties.
     * @param systemProperties A map of keys to system property names to load into extra properties.
     * @return The number of system properties loaded into the extra properties.
     */
    private fun processSystemProps(systemProperties: Map<Ext.Key, Ext.SysPropName>, ext: ExtraPropertiesExtension): Int =
        systemProperties
            .onEach { (key, sysPropName) -> ext[key.value] = System.getProperty(sysPropName.value) }
            .count()
}