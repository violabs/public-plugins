package io.violabs.plugins.open.ai.claude.claudecode.skillresolver.domain

/**
 * Configuration for resolving skills from a GitHub repository.
 */
data class SkillConfig(
    val owner: String,
    val repo: String,
    val branch: String,
    val skillsPath: String,
    val skills: List<String>,
    val outputDirectory: java.io.File,
    val dryRun: Boolean,
    val connectionTimeoutMs: Long,
    val readTimeoutMs: Long
) {
    /**
     * Builds the base URL for the GitHub raw content.
     * Format: https://raw.githubusercontent.com/{owner}/{repo}/{branch}/{skillsPath}
     */
    fun buildBaseUrl(): String {
        //todo: allow full override
        return "https://raw.githubusercontent.com/$owner/$repo/$branch/$skillsPath"
    }

    /**
     * Builds the URL for a specific skill file.
     */
    fun buildSkillFileUrl(skillName: String, fileName: String): String {
        return "${buildBaseUrl()}/$skillName/$fileName"
    }

    /**
     * Validates the configuration and returns any errors.
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        if (owner.isBlank()) {
            errors.add("owner must not be blank")
        }
        if (repo.isBlank()) {
            errors.add("repo must not be blank")
        }
        if (branch.isBlank()) {
            errors.add("branch must not be blank")
        }
        if (skills.isEmpty()) {
            errors.add("at least one skill must be specified")
        }
        skills.forEach { skill ->
            if (!isValidKebabCase(skill)) {
                errors.add("skill name '$skill' must be in kebab-case (lowercase letters, numbers, and hyphens only)")
            }
        }

        return errors
    }

    companion object {
        private val KEBAB_CASE_REGEX = Regex("^[a-z0-9]+(-[a-z0-9]+)*$")

        fun isValidKebabCase(value: String): Boolean {
            return KEBAB_CASE_REGEX.matches(value)
        }
    }
}
