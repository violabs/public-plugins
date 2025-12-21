import io.violabs.plugins.local.publishing.digitalocean.domain.uploadToDigitalOceanSpaces
import io.violabs.plugins.local.publishing.mavengenerated.domain.mavenGeneratedArtifacts
import io.violabs.plugins.local.secrets.getPropertyOrEnv

val claudeCodeSkillResolver: String by rootProject.extra

plugins {
    `kotlin-dsl`
    id("org.jetbrains.dokka")
    id("io.violabs.plugins.local.publishing.maven-generated-artifacts")
    id("io.violabs.plugins.local.publishing.digital-ocean-spaces")
}

group = "io.violabs.plugins.open.ai.claude.claude-code-skill-resolver"
version = claudeCodeSkillResolver

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.0.20"))
    }
}

tasks.jar {
    archiveBaseName.set("claude-code-skill-resolver")
}

repositories {
    // Add any required repositories
    mavenCentral()
}

dependencies {
    testImplementation(gradleTestKit())
    testImplementation(project(":test-core"))
}

gradlePlugin {
    plugins {
        create("claudeCodeSkillResolverPlugin") {
            id = "io.violabs.plugins.open.ai.claude.claude-code-skill-resolver"
            version = version.toString()
            implementationClass = "io.violabs.plugins.open.ai.claude.claudecode.skillresolver.ClaudeCodeSkillResolverPlugin"
        }
    }
}

digitalOceanSpacesPublishing {
    bucket = "open-reliquary"
    accessKey = project.getPropertyOrEnv("spaces.key", "DO_SPACES_API_KEY")
    secretKey = project.getPropertyOrEnv("spaces.secret", "DO_SPACES_SECRET")
    publishedVersion = version.toString()
    isPlugin = true
    dryRun = false
}

tasks.uploadToDigitalOceanSpaces?.apply {
    dependsOn(tasks.mavenGeneratedArtifacts)
}

mavenGeneratedArtifacts {
    publicationName = "claudeCodeSkillResolver"
    name = "Claude Code Skill Resolver"
    description = """
            This plugin will download and copy skills based on url and output structure.
            Allows to choose specific skill names.
        """
    websiteUrl = "https://github.com/violabs/public-plugins/tree/main/ai/claude/claude-code-skill-resolver"

    licenses {
        license {
            name = "Apache License, Version 2.0"
            url = "https://www.apache.org/licenses/LICENSE-2.0"
        }
    }

    developers {
        developer {
            id = "violabs"
            name = "Violabs Team"
            email = "support@violabs.io"
            organization = "Violabs Software"
        }
    }

    scm {
        connection = "https://github.com/violabs/public-plugins.git"
    }
}