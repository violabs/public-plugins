package io.violabs.plugins.open.ai.claude.claudecode.skillresolver.domain

/**
 * Represents a file within a skill directory.
 */
data class SkillFile(
    val skillName: String,
    val fileName: String,
    val content: String,
    val url: String
)
