# Violabs Public Plugins

A collection of open-source Gradle plugins.

## Available Plugins

| Plugin | Description |
|--------|-------------|
| [Claude Code Skill Resolver](ai/claude/claude-code-skill-resolver) | Downloads and installs Claude Code skills from GitHub repositories |
| [Digital Ocean Spaces](publishing/digital-ocean-spaces) | Publishes artifacts (JARs, POMs, Dokka) to DigitalOcean Spaces |
| [Maven Generated Artifacts](publishing/maven-generated-artifacts) | Generates Maven artifacts (sources, Javadoc, KDoc JARs) for publishing |

## Plugin Repository

All plugins are hosted at:
```
https://open-reliquary.nyc3.digitaloceanspaces.com
```

Add this to your `settings.gradle.kts`:

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

## License

Apache License, Version 2.0

---

## Development

Internal notes for maintainers.

### Uploading Plugins

```shell
# Claude Code Skill Resolver
./gradlew :ai:claude:claude-code-skill-resolver:clean :ai:claude:claude-code-skill-resolver:uploadToDigitalOceanSpaces

# Digital Ocean Spaces
./gradlew publishing:digital-ocean-spaces:clean publishing:digital-ocean-spaces:uploadToDigitalOceanSpaces

# Maven Generated Artifacts
./gradlew publishing:maven-generated-artifacts:clean publishing:maven-generated-artifacts:uploadToDigitalOceanSpaces
```