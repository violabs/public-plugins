package io.violabs.plugins.open.ai.claude.claudecode.skillresolver.domain

/**
 * Result of attempting to resolve skills.
 */
sealed class SkillResolutionResult {
    data class Success(
        val resolvedSkills: List<ResolvedSkill>,
        val warnings: List<String> = emptyList()
    ) : SkillResolutionResult() {
        val totalFiles: Int get() = resolvedSkills.sumOf { it.fileCount }
        val skillCount: Int get() = resolvedSkills.size
    }

    data class Failure(
        val errors: List<String>,
        val partialResults: List<ResolvedSkill> = emptyList()
    ) : SkillResolutionResult()
}
