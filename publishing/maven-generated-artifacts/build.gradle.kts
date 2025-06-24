plugins {
    `kotlin-dsl`
    id("io.violabs.plugins.local.publishing.digital-ocean-spaces")
    id("io.violabs.plugins.local.publishing.maven-generated-artifacts")
    id("org.jetbrains.dokka") version "1.9.20"
}

group = "io.violabs.plugins.open.publishing"
version = "0.0.2"

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

gradlePlugin {
    plugins {
        create("mavenGeneratedArtifacts") {
            id = "io.violabs.plugins.open.publishing.maven-generated-artifacts"
            version = project.version.toString()
            implementationClass = "io.violabs.plugins.open.publishing.ManualMavenArtifactsPlugin"
        }
    }
}

digitalOceanSpacesPublishing {
    artifactPath = "io/violabs/plugins/open/publishing/maven-generated-artifacts/$version"
}

mavenGeneratedArtifacts {
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