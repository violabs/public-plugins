package io.violabs.plugins.open.ai.claude.claudecode.skillresolver.service

import io.violabs.plugins.open.ai.claude.claudecode.skillresolver.TestLogger
import io.violabs.plugins.open.ai.claude.claudecode.skillresolver.domain.SkillConfig
import io.violabs.plugins.open.ai.claude.claudecode.skillresolver.domain.SkillResolutionResult
import io.violabs.test.core.UnitTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Integration tests for SkillResolverService that actually fetch from the
 * violabs-josh/claude-public-skills repository and write files to disk.
 */
class SkillResolverServiceIntegrationTest : UnitTest() {

    @TempDir
    lateinit var tempDir: File

    private val logger = TestLogger()

    @Test
    fun `resolveSkills should successfully resolve pdf skill`() {
        val config = createConfig(skills = listOf("pdf"))
        val service = SkillResolverService(config, logger)

        val result = service.resolveSkills()

        assert(result is SkillResolutionResult.Success) {
            "Expected Success but got: $result"
        }

        val success = result as SkillResolutionResult.Success
        success.skillCount eq 1
        assert(success.totalFiles > 0) { "Expected at least one file" }

        val pdfSkill = success.resolvedSkills.find { it.name == "pdf" }
        assert(pdfSkill != null) { "Expected to find pdf skill" }
    }

    @Test
    fun `resolveSkills should successfully resolve multiple skills`() {
        val config = createConfig(skills = listOf("pdf", "docx"))
        val service = SkillResolverService(config, logger)

        val result = service.resolveSkills()

        assert(result is SkillResolutionResult.Success) {
            "Expected Success but got: $result"
        }

        val success = result as SkillResolutionResult.Success
        success.skillCount eq 2
    }

    @Test
    fun `resolveSkills should return warning for non-existent skill`() {
        val config = createConfig(skills = listOf("pdf", "non-existent-skill-xyz"))
        val service = SkillResolverService(config, logger)

        val result = service.resolveSkills()

        assert(result is SkillResolutionResult.Success) {
            "Expected Success with warnings but got: $result"
        }

        val success = result as SkillResolutionResult.Success
        success.skillCount eq 1  // Only pdf was found
        assert(success.warnings.isNotEmpty()) { "Expected warnings for non-existent skill" }
    }

    @Test
    fun `resolveSkills should fail for invalid configuration`() {
        val config = createConfig(owner = "", skills = listOf("pdf"))
        val service = SkillResolverService(config, logger)

        val result = service.resolveSkills()

        assert(result is SkillResolutionResult.Failure) {
            "Expected Failure for invalid config but got: $result"
        }
    }

    @Test
    fun `writeSkills should write files to disk`() {
        val config = createConfig(skills = listOf("pdf"))
        val service = SkillResolverService(config, logger)

        val result = service.resolveSkills()
        assert(result is SkillResolutionResult.Success) { "Expected Success" }

        val success = result as SkillResolutionResult.Success
        val filesWritten = service.writeSkills(success)

        assert(filesWritten > 0) { "Expected at least one file to be written" }

        // Verify the skill directory was created
        val pdfDir = File(tempDir, "pdf")
        assert(pdfDir.exists()) { "Expected pdf directory to exist" }
        assert(pdfDir.isDirectory) { "Expected pdf to be a directory" }

        // Verify files were written
        val files = pdfDir.listFiles()
        assert(files != null && files.isNotEmpty()) { "Expected files in pdf directory" }

        println("Files written to ${pdfDir.absolutePath}:")
        files?.forEach { println("  - ${it.name}") }
    }

    @Test
    fun `writeSkills should not write files in dry run mode`() {
        val config = createConfig(skills = listOf("pdf"), dryRun = true)
        val service = SkillResolverService(config, logger)

        val result = service.resolveSkills()
        assert(result is SkillResolutionResult.Success) { "Expected Success" }

        val success = result as SkillResolutionResult.Success
        val filesWritten = service.writeSkills(success)

        filesWritten eq 0

        // Verify no directory was created
        val pdfDir = File(tempDir, "pdf")
        assert(!pdfDir.exists()) { "Expected pdf directory to NOT exist in dry run mode" }
    }

    @Test
    fun `resolveAndWrite should resolve and write skills in one operation`() {
        val config = createConfig(skills = listOf("pdf"))
        val service = SkillResolverService(config, logger)

        val (success, message) = service.resolveAndWrite()

        assert(success) { "Expected success but got: $message" }
        assert(message.contains("Successfully resolved")) { "Expected success message but got: $message" }

        // Verify files were written
        val pdfDir = File(tempDir, "pdf")
        assert(pdfDir.exists()) { "Expected pdf directory to exist" }
    }

    @Test
    fun `resolveAndWrite should return dry run message in dry run mode`() {
        val config = createConfig(skills = listOf("pdf"), dryRun = true)
        val service = SkillResolverService(config, logger)

        val (success, message) = service.resolveAndWrite()

        assert(success) { "Expected success but got: $message" }
        assert(message.contains("[DRY RUN]")) { "Expected dry run message but got: $message" }
    }

    private fun createConfig(
        owner: String = "violabs-josh",
        repo: String = "claude-public-skills",
        branch: String = "main",
        skillsPath: String = "skills",
        skills: List<String> = listOf("pdf"),
        dryRun: Boolean = false
    ) = SkillConfig(
        owner = owner,
        repo = repo,
        branch = branch,
        skillsPath = skillsPath,
        skills = skills,
        outputDirectory = tempDir,
        dryRun = dryRun,
        connectionTimeoutMs = 30_000L,
        readTimeoutMs = 60_000L
    )
}
