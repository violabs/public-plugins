package io.violabs.plugins.open.publishing.digitalocean

import java.io.File

object TestFileStructureBuilder {
    fun createMavenArtifacts(
        buildDir: File,
        name: String = "my-lib",
        version: String = "1.0.0",
        jarQualifier: String? = null
    ) {
        val libsDir = File(buildDir, "libs").apply { mkdirs() }
        val baseName = jarQualifier ?: name
        
        // Create all artifact types
        listOf("", "-sources", "-javadoc", "-kdoc").forEach { suffix ->
            File(libsDir, "$baseName-$version$suffix.jar").writeText("dummy content")
            File(libsDir, "$baseName-$version$suffix.jar.sha1").writeText("sha1-hash")
            File(libsDir, "$baseName-$version$suffix.jar.sha256").writeText("sha256-hash")
        }
        
        // Create POM
        val publicationsDir = File(buildDir, "publications/maven").apply { mkdirs() }
        File(publicationsDir, "pom-default.xml").writeText("<project/>")
    }
}