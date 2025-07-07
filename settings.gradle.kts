rootProject.name = "public-plugins"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
        maven {
            url = uri("https://open-reliquary.nyc3.digitaloceanspaces.com")
        }
    }
}

include(
    "example",
    "publishing:digital-ocean-spaces",
    "publishing:maven-generated-artifacts",
    "secrets:gradle-loader",
    "test-core"
)