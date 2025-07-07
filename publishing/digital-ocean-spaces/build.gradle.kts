import io.violabs.plugins.local.publishing.digitalocean.domain.uploadToDigitalOceanSpaces
import io.violabs.plugins.local.publishing.mavengenerated.domain.mavenGeneratedArtifacts
import io.violabs.plugins.local.secrets.getPropertyOrEnv

val publishingDigitalOceanSpacesVersion: String by rootProject.extra

plugins {
    `kotlin-dsl`
    id("org.jetbrains.dokka")
    id("io.violabs.plugins.local.publishing.project-sync")
    id("io.violabs.plugins.local.publishing.maven-generated-artifacts")
    id("io.violabs.plugins.local.publishing.digital-ocean-spaces")
}

group = "io.violabs.plugins.open.publishing"
version = publishingDigitalOceanSpacesVersion

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.0.20"))
    }
}

tasks.jar {
    archiveBaseName.set("digital-ocean-spaces")
}


repositories {
    // Add any required repositories
    mavenCentral()
}

dependencies {
    implementation("software.amazon.awssdk:s3:2.25.27")

    testImplementation(gradleTestKit())
    testImplementation(project(":test-core"))
}

gradlePlugin {
    plugins {
        create("digitalOceanSpacesPlugin") {
            id = "io.violabs.plugins.open.publishing.digital-ocean-spaces"
            version = version.toString()
            implementationClass = "io.violabs.plugins.open.publishing.digitalocean.DigitalOceanSpacesPublishPlugin"
        }
    }
}

projectSync {
    autoSync()
    val projectFile = rootProject.layout
        .projectDirectory
        .asFile
    syncSource = projectFile
        .resolve("publishing/digital-ocean-spaces/src/main/kotlin/io/violabs/plugins/open/publishing/digitalocean")
    syncTarget = projectFile
        .resolve("buildSrc/src/main/kotlin/io/violabs/plugins/local/publishing/digitalocean")
}

digitalOceanSpacesPublishing {
    bucket = "open-reliquary"
    accessKey = project.getPropertyOrEnv("spaces.key", "DO_SPACES_API_KEY")
    secretKey = project.getPropertyOrEnv("spaces.secret", "DO_SPACES_SECRET")
    publishedVersion = version.toString()
    isPlugin = true
}

tasks.uploadToDigitalOceanSpaces?.apply {
    dependsOn(tasks.mavenGeneratedArtifacts)
}

mavenGeneratedArtifacts {
    publicationName = "digitalOceanSpaces"
    name = "Digital Ocean Spaces Publishing"
    description = """
            This plugin publishes the build jar, sources jar, pom, and optionally dokka jars.
        """
    websiteUrl = "https://github.com/violabs/public-plugins/tree/main/publishing/digital-ocean-spaces"

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