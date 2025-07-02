plugins {
    `kotlin-dsl`
    id("org.jetbrains.dokka") version "1.9.20"
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