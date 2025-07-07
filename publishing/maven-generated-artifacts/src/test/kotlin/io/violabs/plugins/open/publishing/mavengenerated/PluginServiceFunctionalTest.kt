package io.violabs.plugins.open.publishing.mavengenerated

import io.violabs.test.core.UnitTest
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@Disabled
class PluginServiceFunctionalTest : UnitTest() {

    private fun writeResource(source: String, dir: File, target: String) {
        val text = javaClass
            .getResource("/functional/$source")!!
            .readText()
        File(dir, target).writeText(text)
    }

    @Test
    fun `full assemble produces jars pom and hashes`(@TempDir dir: File) {
        // 1) copy in settings.gradle.kts and build.gradle.test.txt from src/test/resources/functional/
        writeResource("settings.gradle.test.txt", dir, "settings.gradle.kts")
        writeResource("build.gradle.test.txt", dir, "build.gradle.kts")

        // 2) run assembleMavenArtifacts
        GradleRunner.create()
            .withProjectDir(dir)
            .withPluginClasspath()
            .withArguments("assembleMavenArtifacts", "--warning-mode", "none")
            .build()

        dir.resolve("build/libs").listFiles().forEach { println(it.name) }

        val libs = File(dir, "build/libs").listFiles()!!.map { it.name }.toSet()

        val expectedLibs = setOf(
            "test.jar.sha256",
            "test-javadoc.jar.sha1",
            "test-javadoc.jar.sha256",
            "test-kdoc.jar",
            "test.jar.sha1",
            "test-sources.jar",
            "test.jar",
            "test-sources.jar.sha256",
            "test-javadoc.jar",
            "test-sources.jar.sha1",
            "test-kdoc.jar.sha256",
            "test-kdoc.jar.sha1"
        )

        // the main jar + sources/javadoc/kdoc + pom
        testEquals(expectedLibs, libs)

        // and the two hash extensions for each artifact
        listOf("sha1", "sha256").forEach { ext ->
            assertTrue(libs.any { it.endsWith(".$ext") })
        }
    }
}
