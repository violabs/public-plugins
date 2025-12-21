package io.violabs.plugins.open.ai.claude.claudecode.skillresolver

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

/**
 * Extension for configuring the Claude Code Skill Resolver plugin.
 *
 * This extension allows you to configure the GitHub repository from which to fetch skills,
 * as well as which specific skills to download and where to output them.
 *
 * Example usage:
 * ```kotlin
 * claudeCodeSkillResolver {
 *     owner.set("violabs-josh")
 *     repo.set("claude-public-skills")
 *     branch.set("main")  // optional, defaults to "main"
 *     skillsPath.set("skills")  // optional, defaults to "skills"
 *     skills.set(listOf("pdf", "docx", "xlsx"))
 *     outputDirectory.set(file(".claude/skills"))
 * }
 * ```
 */
abstract class ClaudeCodeSkillResolverExtension {
    /**
     * The GitHub repository owner (username or organization).
     * Example: "violabs-josh"
     */
    abstract val owner: Property<String>

    /**
     * The GitHub repository name.
     * Example: "claude-public-skills"
     */
    abstract val repo: Property<String>

    /**
     * The branch to fetch skills from. Defaults to "main".
     */
    abstract val branch: Property<String>

    /**
     * The path within the repository where skills are located. Defaults to "skills".
     */
    abstract val skillsPath: Property<String>

    /**
     * List of skill names to download (kebab-case).
     * Example: listOf("pdf", "docx", "frontend-design")
     */
    abstract val skills: ListProperty<String>

    /**
     * The output directory where skills will be copied.
     * Defaults to "claude/skills" in the project directory.
     */
    abstract val outputDirectory: Property<java.io.File>

    /**
     * Whether to run in dry-run mode (no actual downloads).
     * Defaults to false.
     */
    abstract val dryRun: Property<Boolean>

    /**
     * Connection timeout in milliseconds. Defaults to 30000 (30 seconds).
     */
    abstract val connectionTimeoutMs: Property<Long>

    /**
     * Read timeout in milliseconds. Defaults to 60000 (60 seconds).
     */
    abstract val readTimeoutMs: Property<Long>

    companion object {
        const val NAME = "claudeCodeSkillResolver"
        const val DEFAULT_BRANCH = "main"
        const val DEFAULT_SKILLS_PATH = "skills"
        const val DEFAULT_OUTPUT_DIR = "claude/skills"
        const val DEFAULT_CONNECTION_TIMEOUT_MS = 30_000L
        const val DEFAULT_READ_TIMEOUT_MS = 60_000L
    }
}
