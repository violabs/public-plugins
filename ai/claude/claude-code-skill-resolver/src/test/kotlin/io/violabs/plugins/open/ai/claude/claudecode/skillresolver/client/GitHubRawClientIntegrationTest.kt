package io.violabs.plugins.open.ai.claude.claudecode.skillresolver.client

import io.violabs.plugins.open.ai.claude.claudecode.skillresolver.TestLogger
import io.violabs.plugins.open.ai.claude.claudecode.skillresolver.domain.SkillConfig
import io.violabs.test.core.UnitTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Integration tests for GitHubRawClient that actually fetch from the
 * violabs-josh/claude-public-skills repository.
 */
class GitHubRawClientIntegrationTest : UnitTest() {

    @TempDir
    lateinit var tempDir: File

    private val logger = TestLogger()

    @Test
    fun `fetchSkillFiles should fetch files from pdf skill`() {
        val config = createConfig(skills = listOf("pdf"))
        val client = GitHubRawClient(config, logger)

        val files = client.fetchSkillFiles("pdf")

        assert(files.isNotEmpty()) { "Expected at least one file in pdf skill" }

        val fileNames = files.map { it.fileName }
        println("Found files in pdf skill: $fileNames")

        files.forEach { file ->
            file.skillName eq "pdf"
            assert(file.content.isNotBlank()) { "File ${file.fileName} should have content" }
            assert(file.url.startsWith("https://raw.githubusercontent.com")) { "URL should be a raw GitHub URL" }
        }
    }

    @Test
    fun `fetchSkillFiles should fetch files from docx skill`() {
        val config = createConfig(skills = listOf("docx"))
        val client = GitHubRawClient(config, logger)

        val files = client.fetchSkillFiles("docx")

        assert(files.isNotEmpty()) { "Expected at least one file in docx skill" }
        files.forEach { file ->
            file.skillName eq "docx"
        }
    }

    @Test
    fun `fetchSkillFiles should return empty list for non-existent skill`() {
        val config = createConfig(skills = listOf("non-existent-skill-12345"))
        val client = GitHubRawClient(config, logger)

        val files = client.fetchSkillFiles("non-existent-skill-12345")

        files.size eq 0
    }

    @Test
    fun `fetchSkillFiles should handle frontend-design skill with hyphenated name`() {
        val config = createConfig(skills = listOf("frontend-design"))
        val client = GitHubRawClient(config, logger)

        val files = client.fetchSkillFiles("frontend-design")

        assert(files.isNotEmpty()) { "Expected at least one file in frontend-design skill" }
        files.forEach { file ->
            file.skillName eq "frontend-design"
        }
    }

    private fun createConfig(
        owner: String = "violabs-josh",
        repo: String = "claude-public-skills",
        branch: String = "main",
        skillsPath: String = "skills",
        skills: List<String> = listOf("pdf")
    ) = SkillConfig(
        owner = owner,
        repo = repo,
        branch = branch,
        skillsPath = skillsPath,
        skills = skills,
        outputDirectory = tempDir,
        dryRun = false,
        connectionTimeoutMs = 30_000L,
        readTimeoutMs = 60_000L
    )
}
