# Claude Code Skill Resolver

A Gradle plugin that downloads and installs [Claude Code](https://platform.claude.com/docs/en/agents-and-tools/agent-skills/overview) skills from GitHub repositories.

If you want to get the official supported skills, check out the [Anthropics Base Skills](https://github.com/anthropics/skills) repository.

## What it does

This plugin fetches skill files from a configured GitHub repository and copies them to your project's `.claude/skills` directory, allowing you to share and reuse Claude Code skills across projects.

## Installation

Add the plugin repository to your `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        maven {
            url = uri("https://open-reliquary.nyc3.digitaloceanspaces.com")
        }
        gradlePluginPortal()
    }
}
```

Apply the plugin in your `build.gradle.kts`:

```kotlin
plugins {
    id("io.violabs.plugins.open.ai.claude.claude-code-skill-resolver") version "0.0.2"
}
```

## Configuration

Configure the plugin to point to your skills repository:

```kotlin
claudeCodeSkillResolver {
    owner.set("your-github-username")       // GitHub username or organization
    repo.set("your-skills-repo")            // Repository name
    skills("pdf", "docx")       // Skills to download

    // Optional settings (shown with defaults)
    branch.set("main")                      // Branch to fetch from
    skillsPath.set("skills")                // Path to skills in the repo
    outputDirectory.set(file(".claude/skills"))  // Where to write skills
    dryRun.set(false)                       // Preview without downloading
    connectionTimeoutMs.set(30000L)         // Connection timeout
    readTimeoutMs.set(60000L)               // Read timeout
}
```

## Usage

Download skills by running:

```shell
./gradlew resolveSkills
```

## GitHub Repository Structure

The plugin expects skills to be organized as follows:

```
your-repo/
  skills/
    pdf/
      skill.md
      examples.md
    docx/
      skill.md
    frontend-design/
      skill.md
      components.md
```

## Skill Naming Convention

Skill names must be in kebab-case (lowercase letters, numbers, and hyphens):
- `pdf`
- `docx`
- `frontend-design`
- `web-artifacts-builder`

> Need a reference? Check out the [Anthropics Base Skills](https://github.com/anthropics/skills).

## License

Apache License, Version 2.0