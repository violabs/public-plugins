package io.violabs.plugins.open.ai.claude.claudecode.skillresolver.task

import io.violabs.plugins.open.ai.claude.claudecode.skillresolver.domain.SkillConfig
import io.violabs.plugins.open.ai.claude.claudecode.skillresolver.service.SkillResolverService
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Gradle task for resolving and downloading Claude Code skills from GitHub.
 *
 * This task fetches skill files from a configured GitHub repository and
 * writes them to the specified output directory.
 */
abstract class ResolveSkillsTask : DefaultTask() {

    init {
        group = "claude"
        description = "Resolves and downloads Claude Code skills from GitHub"
    }

    @get:Input
    abstract val owner: Property<String>

    @get:Input
    abstract val repo: Property<String>

    @get:Input
    @get:Optional
    abstract val branch: Property<String>

    @get:Input
    @get:Optional
    abstract val skillsPath: Property<String>

    @get:Input
    abstract val skills: ListProperty<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val dryRun: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val connectionTimeoutMs: Property<Long>

    @get:Input
    @get:Optional
    abstract val readTimeoutMs: Property<Long>

    @TaskAction
    fun resolveSkills() {
        val config = SkillConfig(
            owner = owner.get(),
            repo = repo.get(),
            branch = branch.getOrElse("main"),
            skillsPath = skillsPath.getOrElse("skills"),
            skills = skills.get(),
            outputDirectory = outputDirectory.get().asFile,
            dryRun = dryRun.getOrElse(false),
            connectionTimeoutMs = connectionTimeoutMs.getOrElse(30_000L),
            readTimeoutMs = readTimeoutMs.getOrElse(60_000L)
        )

        logger.lifecycle("=".repeat(60))
        logger.lifecycle("Claude Code Skill Resolver")
        logger.lifecycle("=".repeat(60))

        val service = SkillResolverService(config, logger)
        val (success, message) = service.resolveAndWrite()

        logger.lifecycle("-".repeat(60))
        logger.lifecycle(message)
        logger.lifecycle("=".repeat(60))

        if (!success) {
            throw org.gradle.api.GradleException("Skill resolution failed. See errors above.")
        }
    }

    companion object {
        const val NAME = "resolveSkills"
    }
}
