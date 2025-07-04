package io.violabs.plugins.open.publishing.mavengenerated.service

import io.violabs.plugins.open.publishing.mavengenerated.domain.ManualMavenArtifactsExtension
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import java.io.File
import java.security.MessageDigest


class MavenGeneratedArtifactsPublishPluginService : BuildService<MavenGeneratedArtifactsPublishPluginService.Params>  {
    interface Params : BuildServiceParameters

    override fun getParameters(): Params = object : Params {}

    fun apply(project: Project) = project.run {
        pluginManager.apply("java")
        pluginManager.apply("org.jetbrains.dokka")
        pluginManager.apply("maven-publish")

        val extension = project.extensions.create<ManualMavenArtifactsExtension>("mavenGeneratedArtifacts")

        val sourceSets = project.extensions.getByType<SourceSetContainer>()

        // 1) Sources JAR
        val sourcesJar = tasks.register<Jar>("sourcesJar") {
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }

        // 2) Dokka Javadoc JAR
        val dokkaJavadocJar = tasks.register<Jar>("dokkaJavadocJar") {
            archiveClassifier.set("javadoc")
            from(tasks.named("dokkaJavadoc"))
        }

        // 3) Dokka HTML/KDoc JAR
        val dokkaHtmlJar = tasks.register<Jar>("dokkaHtmlJar") {
            archiveClassifier.set("kdoc")
            from(tasks.named("dokkaHtml"))
        }

        // Configure publishing
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])
                    artifact(sourcesJar)
                    artifact(dokkaJavadocJar)
                    artifact(dokkaHtmlJar)

                    pom {
                        name.set(extension.name)
                        description.set(extension.description?.trimIndent())
                        url.set(extension.websiteUrl)

                        licenses {
                            extension.licenses()?.forEach { license ->
                                license {
                                    name.set(license.name)
                                    url.set(license.url)
                                }
                            }
                        }
                        developers {
                            extension.developers()?.forEach { developer ->
                                developer {
                                    id.set(developer.id)
                                    name.set(developer.name)
                                    email.set(developer.email)
                                    organization.set(developer.organization)
                                }
                            }
                        }
                        scm {
                            val scm = extension.scm()
                            val connectionLocation = scm?.connection ?: "github.com/violabs/${project.name}.git"
                            val developerConnectionLocation = scm?.developerConnection ?: connection
                            connection.set("scm:git:git://$connectionLocation")
                            developerConnection.set("scm:git:ssh://$developerConnectionLocation")
                            url.set(scm?.url ?: extension.websiteUrl)
                        }
                    }
                }
            }
        }

        tasks.register("generateHashes") {
            group = "distribution"
            description = "Generates SHA-256 and SHA-1 hash files for all artifacts."

            doLast {
                val libsDir = file("${layout.buildDirectory.get()}/libs")
                libsDir.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        listOf("SHA-256", "SHA-1").forEach { algo ->
                            val hash = file.generateHash(algo)
                            val ext = when(algo) {
                                "SHA-1" -> "sha1"
                                "SHA-256" -> "sha256"
                                else -> algo.lowercase()
                            }
                            file.resolveSibling("${file.name}.$ext").writeText(hash)
                            logger.lifecycle(" | [INFO] Created file: ${file.name}.$ext")
                        }
                    }
                }
            }
        }

        // 5) Make a single "assembleMavenArtifacts" umbrella task
        tasks.register("assembleMavenArtifacts") {
            dependsOn("jar", sourcesJar, dokkaJavadocJar, dokkaHtmlJar, "generatePomFileForMavenPublication")
            group = "distribution"
            description = "Builds main, sources, javadoc, kdoc jars and the POM."
            finalizedBy("generateHashes")
        }
    }

    fun File.generateHash(hashAlgo: String): String {
        val buffer = ByteArray(1024 * 4)
        val md = MessageDigest.getInstance(hashAlgo)
        inputStream().use { fis ->
            var bytes = fis.read(buffer)
            while (bytes >= 0) {
                if (bytes > 0) md.update(buffer, 0, bytes)
                bytes = fis.read(buffer)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}