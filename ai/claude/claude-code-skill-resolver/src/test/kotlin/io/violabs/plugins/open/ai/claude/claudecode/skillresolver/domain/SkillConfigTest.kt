package io.violabs.plugins.open.ai.claude.claudecode.skillresolver.domain

import io.violabs.test.core.UnitTest
import org.junit.jupiter.api.Test
import java.io.File

class SkillConfigTest : UnitTest() {

    @Test
    fun `buildBaseUrl should construct correct URL`() {
        val config = createConfig()

        config.buildBaseUrl() eq "https://raw.githubusercontent.com/violabs-josh/claude-public-skills/main/skills"
    }

    @Test
    fun `buildBaseUrl should use custom branch`() {
        val config = createConfig(branch = "develop")

        config.buildBaseUrl() eq "https://raw.githubusercontent.com/violabs-josh/claude-public-skills/develop/skills"
    }

    @Test
    fun `buildBaseUrl should use custom skillsPath`() {
        val config = createConfig(skillsPath = "custom/path")

        config.buildBaseUrl() eq "https://raw.githubusercontent.com/violabs-josh/claude-public-skills/main/custom/path"
    }

    @Test
    fun `buildSkillFileUrl should construct correct URL for file`() {
        val config = createConfig()

        config.buildSkillFileUrl("pdf", "README.md") eq
            "https://raw.githubusercontent.com/violabs-josh/claude-public-skills/main/skills/pdf/README.md"
    }

    @Test
    fun `validate should return empty list for valid config`() {
        val config = createConfig()

        config.validate() eq emptyList()
    }

    @Test
    fun `validate should return error for blank owner`() {
        val config = createConfig(owner = "")

        val errors = config.validate()
        errors.size eq 1
        errors[0] eq "owner must not be blank"
    }

    @Test
    fun `validate should return error for blank repo`() {
        val config = createConfig(repo = "   ")

        val errors = config.validate()
        errors.size eq 1
        errors[0] eq "repo must not be blank"
    }

    @Test
    fun `validate should return error for blank branch`() {
        val config = createConfig(branch = "")

        val errors = config.validate()
        errors.size eq 1
        errors[0] eq "branch must not be blank"
    }

    @Test
    fun `validate should return error for empty skills list`() {
        val config = createConfig(skills = emptyList())

        val errors = config.validate()
        errors.size eq 1
        errors[0] eq "at least one skill must be specified"
    }

    @Test
    fun `validate should return error for invalid kebab-case skill name`() {
        val config = createConfig(skills = listOf("InvalidName"))

        val errors = config.validate()
        errors.size eq 1
        assert(errors[0].contains("must be in kebab-case"))
    }

    @Test
    fun `validate should return multiple errors`() {
        val config = createConfig(owner = "", repo = "", skills = listOf("InvalidName"))

        val errors = config.validate()
        errors.size eq 3
    }

    @Test
    fun `isValidKebabCase should accept valid kebab-case names`() {
        validKebabCaseTests()
    }

    private fun validKebabCaseTests() {
        SkillConfig.isValidKebabCase("pdf") eq true
        SkillConfig.isValidKebabCase("docx") eq true
        SkillConfig.isValidKebabCase("frontend-design") eq true
        SkillConfig.isValidKebabCase("web-artifacts-builder") eq true
        SkillConfig.isValidKebabCase("skill123") eq true
        SkillConfig.isValidKebabCase("skill-123-test") eq true
    }

    @Test
    fun `isValidKebabCase should reject invalid names`() {
        invalidKebabCaseTests()
    }

    private fun invalidKebabCaseTests() {
        SkillConfig.isValidKebabCase("InvalidName") eq false
        SkillConfig.isValidKebabCase("UPPERCASE") eq false
        SkillConfig.isValidKebabCase("snake_case") eq false
        SkillConfig.isValidKebabCase("camelCase") eq false
        SkillConfig.isValidKebabCase("PascalCase") eq false
        SkillConfig.isValidKebabCase("has spaces") eq false
        SkillConfig.isValidKebabCase("-starts-with-hyphen") eq false
        SkillConfig.isValidKebabCase("ends-with-hyphen-") eq false
        SkillConfig.isValidKebabCase("double--hyphen") eq false
        SkillConfig.isValidKebabCase("") eq false
    }

    private fun createConfig(
        owner: String = "violabs-josh",
        repo: String = "claude-public-skills",
        branch: String = "main",
        skillsPath: String = "skills",
        skills: List<String> = listOf("pdf", "docx"),
        outputDirectory: File = File("/tmp/output"),
        dryRun: Boolean = false,
        connectionTimeoutMs: Long = 30_000L,
        readTimeoutMs: Long = 60_000L
    ) = SkillConfig(
        owner = owner,
        repo = repo,
        branch = branch,
        skillsPath = skillsPath,
        skills = skills,
        outputDirectory = outputDirectory,
        dryRun = dryRun,
        connectionTimeoutMs = connectionTimeoutMs,
        readTimeoutMs = readTimeoutMs
    )
}
