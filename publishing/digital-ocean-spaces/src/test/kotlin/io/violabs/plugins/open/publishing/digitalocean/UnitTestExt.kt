package io.violabs.plugins.open.publishing.digitalocean

import java.io.File

internal fun createRequiredFileStructure(
    buildDir: File,
    baseName: String = "my-lib",
    isPlugin: Boolean = false
) {
    // Create libs directory
    val libsDir = File(buildDir, "libs").apply { mkdirs() }

    // Create all required JAR files and their checksums
    listOf("", "-sources", "-javadoc", "-kdoc").forEach { suffix ->
        File(libsDir, "$baseName-1.0.0$suffix.jar").writeText("dummy jar content")
        File(libsDir, "$baseName-1.0.0$suffix.jar.sha1").writeText("dummy-sha1")
        File(libsDir, "$baseName-1.0.0$suffix.jar.sha256").writeText("dummy-sha256")
    }

    // Create publications directory and POM file
    File(libsDir, "$baseName-1.0.0.pom").writeText(
        """
            <?xml version="1.0" encoding="UTF-8"?>
            <project>
                <groupId>com.example</groupId>
                <artifactId>my-lib</artifactId>
                <version>1.0.0</version>
            </project>
        """.trimIndent()
    )
    File(libsDir, "$baseName-1.0.0.pom.sha1").writeText("dummy-sha1")
    File(libsDir, "$baseName-1.0.0.pom.sha256").writeText("dummy-sha256")

    if (isPlugin) {
        // make file to handle File(buildDir, "publications/${publication.name}/pom-default.xml")
        val publicationDir = File(buildDir, "publications").apply {
            mkdirs()
        }
        val testDir = File(publicationDir, "digitalOceanSpaces").apply {
            mkdirs()
        }
        File(testDir, "pom-default.xml").writeText(
            """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <project>
                        <groupId>io.violabs</groupId>
                        <artifactId>my-lib</artifactId>
                        <version>1.0.0</version>
                    </project>
                """.trimIndent()
        )

        val pluginBase = "io.violabs.$baseName.gradle.plugin-1.0.0.pom"

        File(libsDir, "$pluginBase.sha1").writeText("dummy-sha1")
        File(libsDir, "$pluginBase.sha256").writeText("dummy-sha256")
        File(libsDir, "$pluginBase.jar").writeText(
            """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <groupId>io.violabs</groupId>
                    <artifactId>$baseName.gradle.plugin</artifactId>
                    <version>1.0.0</version>
                    <name>My Lib Gradle Plugin</name>
                    <description>A Gradle plugin for My Lib</description>
                </project>
            """.trimIndent()
        )
    }
}
