package io.violabs.plugins.open.ai.claude.claudecode.skillresolver

import io.violabs.plugins.open.ai.claude.claudecode.skillresolver.task.ResolveSkillsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

/**
 * Gradle plugin for resolving and downloading Claude Code skills from GitHub repositories.
 *
 * This plugin allows you to configure a GitHub repository as a source for Claude Code skills,
 * specify which skills to download, and automatically fetch and install them to your project.
 *
 * ## Usage
 *
 * Apply the plugin in your `build.gradle.kts`:
 *
 * ```kotlin
 * plugins {
 *     id("io.violabs.plugins.open.ai.claude.claude-code-skill-resolver") version "0.0.1"
 * }
 *
 * claudeCodeSkillResolver {
 *     owner.set("violabs-josh")
 *     repo.set("claude-public-skills")
 *     branch.set("main")  // optional, defaults to "main"
 *     skillsPath.set("skills")  // optional, defaults to "skills"
 *     skills.set(listOf("pdf", "docx", "xlsx", "frontend-design"))
 *     outputDirectory.set(file(".claude/skills"))  // optional
 * }
 * ```
 *
 * Then run:
 * ```
 * ./gradlew resolveSkills
 * ```
 *
 * ## Skill Naming Convention
 *
 * Skills must be named in kebab-case (lowercase letters, numbers, and hyphens).
 * Examples: `pdf`, `docx`, `frontend-design`, `web-artifacts-builder`
 *
 * ## GitHub Repository Structure
 *
 * The plugin expects skills to be organized in the repository as follows:
 * ```
 * {repo}/
 *   {skillsPath}/
 *     skill-name-1/
 *       file1.md
 *       file2.md
 *     skill-name-2/
 *       file1.md
 * ```
 */
class ClaudeCodeSkillResolverPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Create the extension
        val extension = project.extensions.create(
            ClaudeCodeSkillResolverExtension.NAME,
            ClaudeCodeSkillResolverExtension::class.java
        )

        // Set default values using convention
        extension.branch.convention(ClaudeCodeSkillResolverExtension.DEFAULT_BRANCH)
        extension.skillsPath.convention(ClaudeCodeSkillResolverExtension.DEFAULT_SKILLS_PATH)
        extension.outputDirectory.convention(
            File(project.projectDir, ClaudeCodeSkillResolverExtension.DEFAULT_OUTPUT_DIR)
        )
        extension.dryRun.convention(false)
        extension.connectionTimeoutMs.convention(ClaudeCodeSkillResolverExtension.DEFAULT_CONNECTION_TIMEOUT_MS)
        extension.readTimeoutMs.convention(ClaudeCodeSkillResolverExtension.DEFAULT_READ_TIMEOUT_MS)
        extension.skills.convention(emptyList())

        // Register the task after project evaluation to ensure extension is configured
        project.afterEvaluate {
            val taskProvider = project.tasks.register(ResolveSkillsTask.NAME, ResolveSkillsTask::class.java)
            taskProvider.configure {
                owner.set(extension.owner)
                repo.set(extension.repo)
                branch.set(extension.branch)
                skillsPath.set(extension.skillsPath)
                skills.set(extension.skills)
                outputDirectory.set(
                    project.layout.projectDirectory.dir(
                        extension.outputDirectory.map { file -> file.absolutePath }
                    )
                )
                dryRun.set(extension.dryRun)
                connectionTimeoutMs.set(extension.connectionTimeoutMs)
                readTimeoutMs.set(extension.readTimeoutMs)
            }
        }

        project.logger.info("Claude Code Skill Resolver plugin applied")
    }
}
