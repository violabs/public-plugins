package io.violabs.plugins.open.ai.claude.claudecode.skillresolver.client

import io.violabs.plugins.open.ai.claude.claudecode.skillresolver.domain.SkillConfig
import io.violabs.plugins.open.ai.claude.claudecode.skillresolver.domain.SkillFile
import org.gradle.api.logging.LogLevel
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
            return logEmptyRequest("No files found for skill: $skillName", LogLevel.WARN)
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

        val request: HttpRequest = buildGetRequest {
            uri(apiUrl)
            withDefaultTimeout()
            headers(
                "Accept" to "application/vnd.github.v3+json",
                "User-Agent" to "ClaudeCodeSkillResolver/1.0"
            )
        }

        return try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            when (response.statusCode()) {
                200  -> parseGitHubDirectoryListing(response.body())
                404  -> logEmptyRequest("Skill directory not found: $skillName", LogLevel.WARN)
                403  -> logEmptyRequest("GitHub API rate limit exceeded or access denied for: $skillName")
                else -> logEmptyRequest("Failed to list skill files: HTTP ${response.statusCode()}")
            }
        } catch (e: Exception) {
            logEmptyRequest("Error listing skill files for $skillName: ${e.message}")
        }
    }

    /**
     * Fetches the raw content of a file from GitHub.
     */
    private fun fetchFileContent(skillName: String, fileInfo: GitHubFileInfo): SkillFile? {
        if (fileInfo.type != "file") {
            return logMissingRequest("Skipping non-file: ${fileInfo.name} (type: ${fileInfo.type})", LogLevel.INFO)
        }

        val rawUrl = config.buildSkillFileUrl(skillName, fileInfo.name)
        logger.info("  | Fetching file: ${fileInfo.name} from $rawUrl")

        //todo: swap with builder
        val request: HttpRequest = buildGetRequest {
            uri(rawUrl)
            withDefaultTimeout()
            headers("User-Agent" to "ClaudeCodeSkillResolver/1.0")
        }

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
                404 -> logMissingRequest("✗ ${fileInfo.name} (not found)", LogLevel.WARN)
                else -> logMissingRequest("✗ ${fileInfo.name} (HTTP ${response.statusCode()})")
            }
        } catch (e: Exception) {
            logMissingRequest("✗ ${fileInfo.name} (${e.message})")
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

    private fun buildGetRequest(scope: HttpRequest.Builder.() -> Unit): HttpRequest {
        return HttpRequest.newBuilder().apply(scope).GET().build()
    }

    private fun HttpRequest.Builder.uri(uri: String): HttpRequest.Builder {
        return this.uri(URI.create(uri))
    }

    private fun HttpRequest.Builder.withDefaultTimeout(): HttpRequest.Builder {
        timeout(Duration.ofMillis(config.readTimeoutMs))
        return this
    }

    private fun HttpRequest.Builder.headers(vararg headerPairs: Pair<String, String>): HttpRequest.Builder {
        headerPairs.forEach { (key, value) -> header(key, value) }
        return this
    }

    private fun <T> logMissingRequest(message: String, logLevel: LogLevel = LogLevel.ERROR): T? {
        logger.log(logLevel, "  | $message")
        return null
    }

    private fun <T> logEmptyRequest(message: String, logLevel: LogLevel = LogLevel.ERROR): List<T> {
        logger.log(logLevel, "  | $message")
        return emptyList()
    }

    /**
     * Simple data class for GitHub file info.
     */
    private data class GitHubFileInfo(
        val name: String,
        val type: String
    )
}
