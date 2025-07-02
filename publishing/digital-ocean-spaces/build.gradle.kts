import io.violabs.plugins.local.secrets.getPropertyOrEnv

plugins {
    `kotlin-dsl`
    id("org.jetbrains.dokka") version "1.9.20"
    id("io.violabs.plugins.local.publishing.digital-ocean-spaces")
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.0.20"))
    }
}

repositories {
    // Add any required repositories
    mavenCentral()
}

dependencies {
    implementation("software.amazon.awssdk:s3:2.25.27")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.mockk:mockk:1.13.8")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("digitalOceanSpacesPlugin") {
            id = "io.violabs.plugins.open.publishing.digital-ocean-spaces"
            version = "0.0.1"
            implementationClass = "io.violabs.plugins.open.publishing.digitalocean.DigitalOceanSpacesPublishPlugin"
        }
    }
}

digitalOceanSpacesPublishing {
    artifactPath = "plugins/io/violabs/plugins/open/publishing/digital-ocean-spaces/$version"
    bucket = "open-reliquary"
    accessKey = project.getPropertyOrEnv("spaces.key", "DO_SPACES_API_KEY")
    secretKey = project.getPropertyOrEnv("spaces.secret", "DO_SPACES_SECRET")
    isPlugin = true
}

mavenGeneratedArtifacts {
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