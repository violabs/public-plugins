import io.violabs.plugins.local.secrets.gradleloader.domain.getPropertyOrEnv

val secretsGradleLoaderVersion: String by rootProject.extra

plugins {
    `kotlin-dsl`
    id("org.jetbrains.dokka")
    id("io.violabs.plugins.local.publishing.project-sync")
    id("io.violabs.plugins.local.publishing.maven-generated-artifacts")
    id("io.violabs.plugins.local.publishing.digital-ocean-spaces")
}

group = "io.violabs.plugins.open.secrets"
version = secretsGradleLoaderVersion

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.0.20"))
    }
}

tasks.jar {
    archiveBaseName.set("gradle-loader")
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
        create("secretsLoaderPlugin") {
            id = "io.violabs.plugins.open.secrets.gradle-loader"
            version = version.toString()
            implementationClass = "io.violabs.plugins.open.secrets.gradleloader.plugin.SecretsGradleLoaderPlugin"
        }
    }
}

projectSync {
    autoSync()
    val projectFile = rootProject.layout
        .projectDirectory
        .asFile
    syncSource = projectFile
        .resolve("secrets/gradle-loader/src/main/kotlin/io/violabs/plugins/open/secrets/gradleloader")
    syncTarget = projectFile
        .resolve("buildSrc/src/main/kotlin/io/violabs/plugins/local/secrets/gradleloader")
}

digitalOceanSpacesPublishing {
    bucket = "open-reliquary"
    accessKey = project.getPropertyOrEnv("spaces.key", "DO_SPACES_API_KEY")
    secretKey = project.getPropertyOrEnv("spaces.secret", "DO_SPACES_SECRET")
    publishedVersion = version.toString()
    isPlugin = true
    dryRun = false
}

mavenGeneratedArtifacts {
    publicationName = "digitalOceanSpaces"
    name = "Secrets Gradle Loader Plugin"
    description = """
            A plugin that will read from a file containing secrets and load them into the Gradle project.
        """
    websiteUrl = "https://github.com/violabs/public-plugins/tree/main/secrets/gradle-loader"

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