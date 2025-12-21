package io.violabs.plugins.open.ai.claude.claudecode.skillresolver

import io.violabs.test.core.UnitTest
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Functional tests for the Claude Code Skill Resolver plugin using GradleRunner.
 * These tests verify the plugin works correctly when applied to a Gradle project.
 */
class ClaudeCodeSkillResolverPluginFunctionalTest : UnitTest() {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `plugin should apply successfully`() {
        val buildFile = File(tempDir, "build.gradle.kts")
        buildFile.writeText("""
            plugins {
                id("io.violabs.plugins.open.ai.claude.claude-code-skill-resolver")
            }

            claudeCodeSkillResolver {
                owner.set("violabs-josh")
                repo.set("claude-public-skills")
                skills.set(listOf("pdf"))
            }
        """.trimIndent())

        val settingsFile = File(tempDir, "settings.gradle.kts")
        settingsFile.writeText("""
            rootProject.name = "test-project"
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withPluginClasspath()
            .withArguments("tasks", "--all")
            .build()

        assert(result.output.contains("resolveSkills")) {
            "Expected resolveSkills task to be registered"
        }
    }

    @Test
    fun `resolveSkills task should fetch and write skills`() {
        val buildFile = File(tempDir, "build.gradle.kts")
        buildFile.writeText("""
            plugins {
                id("io.violabs.plugins.open.ai.claude.claude-code-skill-resolver")
            }

            claudeCodeSkillResolver {
                owner.set("violabs-josh")
                repo.set("claude-public-skills")
                skills.set(listOf("pdf"))
                outputDirectory.set(file(".claude/skills"))
            }
        """.trimIndent())

        val settingsFile = File(tempDir, "settings.gradle.kts")
        settingsFile.writeText("""
            rootProject.name = "test-project"
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withPluginClasspath()
            .withArguments("resolveSkills", "--stacktrace")
            .build()

        result.task(":resolveSkills")?.outcome eq TaskOutcome.SUCCESS

        // Verify the output directory was created
        val outputDir = File(tempDir, ".claude/skills/pdf")
        assert(outputDir.exists()) { "Expected output directory to exist: ${outputDir.absolutePath}" }
        assert(outputDir.isDirectory) { "Expected output to be a directory" }

        val files = outputDir.listFiles()
        assert(files != null && files.isNotEmpty()) { "Expected files in output directory" }

        println("Resolved files:")
        files?.forEach { println("  - ${it.name}") }
    }

    @Test
    fun `resolveSkills task should work with multiple skills`() {
        val buildFile = File(tempDir, "build.gradle.kts")
        buildFile.writeText("""
            plugins {
                id("io.violabs.plugins.open.ai.claude.claude-code-skill-resolver")
            }

            claudeCodeSkillResolver {
                owner.set("violabs-josh")
                repo.set("claude-public-skills")
                skills.set(listOf("pdf", "docx"))
                outputDirectory.set(file(".claude/skills"))
            }
        """.trimIndent())

        val settingsFile = File(tempDir, "settings.gradle.kts")
        settingsFile.writeText("""
            rootProject.name = "test-project"
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withPluginClasspath()
            .withArguments("resolveSkills")
            .build()

        result.task(":resolveSkills")?.outcome eq TaskOutcome.SUCCESS

        // Verify both skill directories were created
        val pdfDir = File(tempDir, ".claude/skills/pdf")
        val docxDir = File(tempDir, ".claude/skills/docx")

        assert(pdfDir.exists()) { "Expected pdf directory to exist" }
        assert(docxDir.exists()) { "Expected docx directory to exist" }
    }

    @Test
    fun `resolveSkills task should work in dry run mode`() {
        val buildFile = File(tempDir, "build.gradle.kts")
        buildFile.writeText("""
            plugins {
                id("io.violabs.plugins.open.ai.claude.claude-code-skill-resolver")
            }

            claudeCodeSkillResolver {
                owner.set("violabs-josh")
                repo.set("claude-public-skills")
                skills.set(listOf("pdf"))
                outputDirectory.set(file(".claude/skills"))
                dryRun.set(true)
            }
        """.trimIndent())

        val settingsFile = File(tempDir, "settings.gradle.kts")
        settingsFile.writeText("""
            rootProject.name = "test-project"
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withPluginClasspath()
            .withArguments("resolveSkills")
            .build()

        result.task(":resolveSkills")?.outcome eq TaskOutcome.SUCCESS
        assert(result.output.contains("[DRY RUN]")) { "Expected dry run output" }

        // Verify no files were written in dry run mode
        val outputDir = File(tempDir, ".claude/skills/pdf")
        assert(!outputDir.exists()) { "Expected NO output directory in dry run mode" }
    }

    @Test
    fun `resolveSkills task should use default branch main`() {
        val buildFile = File(tempDir, "build.gradle.kts")
        buildFile.writeText("""
            plugins {
                id("io.violabs.plugins.open.ai.claude.claude-code-skill-resolver")
            }

            claudeCodeSkillResolver {
                owner.set("violabs-josh")
                repo.set("claude-public-skills")
                // branch not set - should default to "main"
                skills.set(listOf("pdf"))
                outputDirectory.set(file(".claude/skills"))
            }
        """.trimIndent())

        val settingsFile = File(tempDir, "settings.gradle.kts")
        settingsFile.writeText("""
            rootProject.name = "test-project"
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withPluginClasspath()
            .withArguments("resolveSkills")
            .build()

        result.task(":resolveSkills")?.outcome eq TaskOutcome.SUCCESS

        // If it succeeded, the default branch worked
        val outputDir = File(tempDir, ".claude/skills/pdf")
        assert(outputDir.exists()) { "Expected output directory to exist (default branch worked)" }
    }

    @Test
    fun `resolveSkills task should fail gracefully for missing required config`() {
        val buildFile = File(tempDir, "build.gradle.kts")
        buildFile.writeText("""
            plugins {
                id("io.violabs.plugins.open.ai.claude.claude-code-skill-resolver")
            }

            claudeCodeSkillResolver {
                // Missing owner and repo
                skills.set(listOf("pdf"))
            }
        """.trimIndent())

        val settingsFile = File(tempDir, "settings.gradle.kts")
        settingsFile.writeText("""
            rootProject.name = "test-project"
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withPluginClasspath()
            .withArguments("resolveSkills")
            .buildAndFail()

        result.task(":resolveSkills")?.outcome eq TaskOutcome.FAILED
    }
}
