package io.violabs.plugins.open.ai.claude.claudecode.skillresolver.service

import io.violabs.plugins.open.ai.claude.claudecode.skillresolver.client.GitHubRawClient
import io.violabs.plugins.open.ai.claude.claudecode.skillresolver.domain.ResolvedSkill
import io.violabs.plugins.open.ai.claude.claudecode.skillresolver.domain.SkillConfig
import io.violabs.plugins.open.ai.claude.claudecode.skillresolver.domain.SkillResolutionResult
import org.gradle.api.logging.Logger
import java.io.File

/**
 * Service for resolving and downloading skills from GitHub repositories.
 */
class SkillResolverService(
    private val config: SkillConfig,
    private val logger: Logger
) {
    private val client: GitHubRawClient by lazy {
        GitHubRawClient(config, logger)
    }

    /**
     * Resolves all configured skills from GitHub.
     *
     * @return The result of the resolution, either Success or Failure
     */
    fun resolveSkills(): SkillResolutionResult {
        // Validate configuration
        val validationErrors = config.validate()
        if (validationErrors.isNotEmpty()) {
            return SkillResolutionResult.Failure(validationErrors)
        }

        logger.lifecycle("Resolving ${config.skills.size} skill(s) from ${config.owner}/${config.repo}@${config.branch}")
        logger.lifecycle("Skills path: ${config.skillsPath}")
        logger.lifecycle("Output directory: ${config.outputDirectory.absolutePath}")

        if (config.dryRun) {
            logger.lifecycle("[DRY RUN] No files will be written")
        }

        val resolvedSkills = mutableListOf<ResolvedSkill>()
        val warnings = mutableListOf<String>()
        val errors = mutableListOf<String>()

        config.skills.forEach { skillName ->
            try {
                val files = client.fetchSkillFiles(skillName)
                if (files.isEmpty()) {
                    warnings.add("Skill '$skillName' has no files or was not found")
                } else {
                    resolvedSkills.add(ResolvedSkill(skillName, files))
                }
            } catch (e: Exception) {
                errors.add("Failed to resolve skill '$skillName': ${e.message}")
                logger.error("Error resolving skill '$skillName'", e)
            }
        }

        return if (errors.isNotEmpty()) {
            SkillResolutionResult.Failure(errors, resolvedSkills)
        } else {
            SkillResolutionResult.Success(resolvedSkills, warnings)
        }
    }

    /**
     * Writes resolved skills to the output directory.
     *
     * @param result The successful resolution result
     * @return Number of files written
     */
    fun writeSkills(result: SkillResolutionResult.Success): Int {
        if (config.dryRun) {
            logger.lifecycle("[DRY RUN] Would write ${result.totalFiles} file(s) to ${config.outputDirectory.absolutePath}")
            result.resolvedSkills.forEach { skill ->
                logger.lifecycle("[DRY RUN]   ${skill.name}/")
                skill.files.forEach { file ->
                    logger.lifecycle("[DRY RUN]     ${file.fileName}")
                }
            }
            return 0
        }

        var filesWritten = 0

        result.resolvedSkills.forEach { skill ->
            val skillDir = File(config.outputDirectory, skill.name)

            if (!skillDir.exists()) {
                skillDir.mkdirs()
                logger.lifecycle("Created directory: ${skillDir.absolutePath}")
            }

            skill.files.forEach { file ->
                val outputFile = File(skillDir, file.fileName)
                outputFile.writeText(file.content)
                filesWritten++
                logger.lifecycle("  Written: ${skill.name}/${file.fileName}")
            }
        }

        return filesWritten
    }

    /**
     * Resolves skills and writes them to disk in one operation.
     *
     * @return Pair of (success, message)
     */
    fun resolveAndWrite(): Pair<Boolean, String> {
        return when (val result = resolveSkills()) {
            is SkillResolutionResult.Success -> {
                val filesWritten = writeSkills(result)

                val warningText = if (result.warnings.isNotEmpty()) {
                    "\nWarnings:\n${result.warnings.joinToString("\n") { "  - $it" }}"
                } else ""

                val message = if (config.dryRun) {
                    "[DRY RUN] Would resolve ${result.skillCount} skill(s) with ${result.totalFiles} file(s)$warningText"
                } else {
                    "Successfully resolved ${result.skillCount} skill(s), wrote $filesWritten file(s)$warningText"
                }

                Pair(true, message)
            }
            is SkillResolutionResult.Failure -> {
                val errorText = "Errors:\n${result.errors.joinToString("\n") { "  - $it" }}"
                Pair(false, "Failed to resolve skills.\n$errorText")
            }
        }
    }
}
