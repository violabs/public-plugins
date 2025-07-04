package io.violabs.plugins.open.publishing.mavengenerated

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class FunctionalPluginTest {
    @Test
    fun `plugin applies happy path`(@TempDir dir: File) {
        // 1) Create a minimal build file
        File(dir, "settings.gradle.kts").writeText("""rootProject.name = "test"""")
        File(dir, "build.gradle.kts").writeText(
            """
              plugins {
                id("io.violabs.plugins.open.publishing.maven-generated-artifacts")
                id("org.jetbrains.dokka") version "1.9.20"
              }
              repositories {
                mavenCentral()
                gradlePluginPortal()
              }
            """.trimIndent()
        )

        // 2) Run Gradle
        val result = GradleRunner.create()
            .withProjectDir(dir)
            .withPluginClasspath()
            .withArguments("tasks")
            .build()

        // 3) Assert that Dokka tasks show up
        assert(result.output.contains("dokkaHtml"))
    }
}