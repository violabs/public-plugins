import io.violabs.plugins.local.secrets.getPropertyOrEnv

val publishingMavenGeneratedArtifactsVersion: String by rootProject.extra

plugins {
    `kotlin-dsl`
    id("org.jetbrains.dokka")
    id("io.violabs.plugins.local.publishing.project-sync")
    id("io.violabs.plugins.local.publishing.maven-generated-artifacts")
    id("io.violabs.plugins.local.publishing.digital-ocean-spaces")
}

group = "io.violabs.plugins.open.publishing"
version = publishingMavenGeneratedArtifactsVersion

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.0.20"))
    }
}

tasks.jar {
    archiveBaseName.set("maven-generated-artifacts")
}

repositories {
    // Add any required repositories
    mavenCentral()
}

dependencies {
    testImplementation(gradleTestKit())
    testImplementation(project(":test-core"))
}

projectSync {
    autoSync()
    val projectFile = rootProject.layout
        .projectDirectory
        .asFile
    syncSource = projectFile
        .resolve("publishing/maven-generated-artifacts/src/main/kotlin/io/violabs/plugins/open/publishing/mavengenerated")
    syncTarget = projectFile
        .resolve("buildSrc/src/main/kotlin/io/violabs/plugins/local/publishing/mavengenerated")
}

gradlePlugin {
    plugins {
        create("mavenGeneratedArtifactsPlugin") {
            id = "io.violabs.plugins.open.publishing.maven-generated-artifacts"
            version = project.version.toString()
            implementationClass = "io.violabs.plugins.open.publishing.mavengenerated.MavenGeneratedArtifactsPlugin"
        }
    }
}

digitalOceanSpacesPublishing {
    bucket = "open-reliquary"
    accessKey = project.getPropertyOrEnv("spaces.key", "DO_SPACES_API_KEY")
    secretKey = project.getPropertyOrEnv("spaces.secret", "DO_SPACES_SECRET")
    artifactPath = "plugins/io/violabs/plugins/open/publishing/maven-generated-artifacts/$version"
    publishedVersion = version.toString()
    isPlugin = true
    dryRun = true
}

mavenGeneratedArtifacts {
    publicationName = "digitalOceanSpaces"
    name = "Maven Generated Artifacts"
    description = """
            This plugin generates Maven artifacts such as sources, Javadoc, and KDoc JARs.
            It is used to publish these artifacts to a Maven repository or a digital ocean space.
        """
    websiteUrl = "https://github.com/violabs/public-plugins/tree/main/publishing/maven-generated-artifacts"

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