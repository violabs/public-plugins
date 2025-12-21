package io.violabs.plugins.open.ai.claude.claudecode.skillresolver.client

import io.violabs.plugins.open.ai.claude.claudecode.skillresolver.domain.SkillConfig
import io.violabs.plugins.open.ai.claude.claudecode.skillresolver.domain.SkillFile
import org.gradle.api.logging.Logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * Client for fetching skill files from GitHub repositories.
 *
 * Uses the GitHub API to list directory contents and then fetches
 * raw file content from raw.githubusercontent.com.
 */
class GitHubRawClient(
    private val config: SkillConfig,
    private val logger: Logger
) {
    private val httpClient: HttpClient by lazy {
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(config.connectionTimeoutMs))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()
    }

    /**
     * Fetches all files for a skill from GitHub.
     *
     * @param skillName The name of the skill (kebab-case directory name)
     * @return List of skill files, or empty list if skill not found
     */
    fun fetchSkillFiles(skillName: String): List<SkillFile> {
        logger.lifecycle("  | Fetching skill: $skillName")

        val files = listSkillFiles(skillName)
        if (files.isEmpty()) {
            logger.warn("  | No files found for skill: $skillName")
            return emptyList()
        }

        logger.lifecycle("  | Found ${files.size} file(s) in skill: $skillName")

        return files.mapNotNull { fileInfo ->
            fetchFileContent(skillName, fileInfo)
        }
    }

    /**
     * Lists files in a skill directory using GitHub API.
     */
    private fun listSkillFiles(skillName: String): List<GitHubFileInfo> {
        val apiUrl = buildGitHubApiUrl(skillName)
        logger.info("  | Listing files from: $apiUrl")

        //todo: extract create
        val request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .timeout(Duration.ofMillis(config.readTimeoutMs))
            .header("Accept", "application/vnd.github.v3+json")
            .header("User-Agent", "ClaudeCodeSkillResolver/1.0")
            .GET()
            .build()

        return try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            when (response.statusCode()) {
                200 -> parseGitHubDirectoryListing(response.body())
                404 -> {
                    logger.warn("  | Skill directory not found: $skillName")
                    emptyList()
                }
                403 -> {
                    //todo: refactor for combined effort
                    logger.error("  | GitHub API rate limit exceeded or access denied for: $skillName")
                    emptyList()
                }
                else -> {
                    logger.error("  | Failed to list skill files: HTTP ${response.statusCode()}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            logger.error("  | Error listing skill files for $skillName: ${e.message}")
            emptyList()
        }
    }

    /**
     * Fetches the raw content of a file from GitHub.
     */
    private fun fetchFileContent(skillName: String, fileInfo: GitHubFileInfo): SkillFile? {
        if (fileInfo.type != "file") {
            logger.info("  | Skipping non-file: ${fileInfo.name} (type: ${fileInfo.type})")
            return null
        }

        val rawUrl = config.buildSkillFileUrl(skillName, fileInfo.name)
        logger.info("  | Fetching file: ${fileInfo.name} from $rawUrl")

        //todo: swap with builder
        val request = HttpRequest.newBuilder()
            .uri(URI.create(rawUrl))
            .timeout(Duration.ofMillis(config.readTimeoutMs))
            .header("User-Agent", "ClaudeCodeSkillResolver/1.0")
            .GET()
            .build()

        return try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            when (response.statusCode()) {
                200 -> {
                    logger.lifecycle("  |   ✓ ${fileInfo.name}")
                    SkillFile(
                        skillName = skillName,
                        fileName = fileInfo.name,
                        content = response.body(),
                        url = rawUrl
                    )
                }
                404 -> {
                    logger.warn("  |   ✗ ${fileInfo.name} (not found)")
                    null
                }
                else -> {
                    logger.error("  |   ✗ ${fileInfo.name} (HTTP ${response.statusCode()})")
                    null
                }
            }
        } catch (e: Exception) {
            logger.error("  |   ✗ ${fileInfo.name} (${e.message})")
            null
        }
    }

    /**
     * Builds the GitHub API URL for listing directory contents.
     * Format: https://api.github.com/repos/{owner}/{repo}/contents/{path}?ref={branch}
     */
    private fun buildGitHubApiUrl(skillName: String): String {
        //todo: add skillName format verification
        val path = "${config.skillsPath}/$skillName"
        return "https://api.github.com/repos/${config.owner}/${config.repo}/contents/$path?ref=${config.branch}"
    }

    /**
     * Parses the GitHub API response for directory listing.
     * The response is a JSON array of file objects.
     *
     * GitHub API returns objects like:
     * {
     *   "name": "file.md",
     *   "type": "file",
     *   "_links": { ... }
     * }
     *
     * We need to handle nested objects like _links, so we split by top-level
     * object boundaries and parse each object individually.
     */
    private fun parseGitHubDirectoryListing(json: String): List<GitHubFileInfo> {
        val files = mutableListOf<GitHubFileInfo>()

        // Split JSON array into individual objects by finding top-level object boundaries
        // We track brace depth to find where each object ends
        val objects = splitJsonArrayIntoObjects(json)

        for (obj in objects) {
            val name = extractJsonStringValue(obj, "name")
            val type = extractJsonStringValue(obj, "type")

            if (name != null && type != null) {
                files.add(GitHubFileInfo(name, type))
            }
        }

        return files
    }

    /**
     * Splits a JSON array string into individual object strings.
     * Handles nested objects by tracking brace depth.
     */
    private fun splitJsonArrayIntoObjects(json: String): List<String> {
        val objects = mutableListOf<String>()
        var depth = 0
        var objectStart = -1

        for (i in json.indices) {
            when (json[i]) {
                '{' -> {
                    if (depth == 0) {
                        objectStart = i
                    }
                    depth++
                }
                '}' -> {
                    depth--
                    if (depth == 0 && objectStart >= 0) {
                        objects.add(json.substring(objectStart, i + 1))
                        objectStart = -1
                    }
                }
            }
        }

        return objects
    }

    /**
     * Extracts a string value from a JSON object for a given key.
     * Only matches top-level keys (not nested ones).
     */
    private fun extractJsonStringValue(json: String, key: String): String? {
        // Pattern to match "key": "value" - handles escaped quotes in values
        val pattern = """"$key"\s*:\s*"([^"\\]*(?:\\.[^"\\]*)*)"""".toRegex()
        return pattern.find(json)?.groupValues?.get(1)
    }

    /**
     * Simple data class for GitHub file info.
     */
    private data class GitHubFileInfo(
        val name: String,
        val type: String
    )
}
