package io.violabs.plugins.open.secrets.gradleloader.domain

import org.gradle.api.Project

/**
 * Extension functions for the [Project] class to retrieve access ID and secret key
 * from either project properties or environment variables.
 * These functions are useful for tasks that require
 * access to sensitive information like API keys or credentials.
 * @param propertyLookup The name of the property to look up in the project.
 * @param envLookup The name of the environment variable to look up if the property is not found.
 * @return The value of the property or environment variable, or null if not found.
 */
fun Project.getPropertyOrEnv(
    propertyLookup: String,
    envLookup: String? = null
): String? {
    logger.lifecycle(" | [INFO] Looking for property '$propertyLookup' or environment variable '$envLookup'")
    return getBasedOnProjectProperty(propertyLookup) ?: getBasedOnEnvVariable(envLookup ?: propertyLookup).also {
        if (it.isNullOrBlank()) {
            logger.lifecycle(" | [WARN] No value found for '$propertyLookup' or environment variable '$envLookup'")
        }
    }
}

private fun Project.getBasedOnProjectProperty(propertyLookup: String): String? {
    val item = (findProperty(propertyLookup) as String?)

    if (!item.isNullOrBlank()) {
        logger.lifecycle(" | [INFO] Found property '$propertyLookup'")
    }

    return item
}

private fun Project.getBasedOnEnvVariable(envLookup: String): String? {
    val item = System.getenv(envLookup)

    if (!item.isNullOrBlank()) {
        logger.lifecycle(" | [INFO] Found environment variable '$envLookup'")
    }

    return item
}
