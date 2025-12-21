package io.violabs.plugins.open.ai.claude.claudecode.skillresolver.domain

/**
 * Represents a successfully resolved skill with all its files.
 */
data class ResolvedSkill(
    val name: String,
    val files: List<SkillFile>
) {
    val fileCount: Int get() = files.size
}
