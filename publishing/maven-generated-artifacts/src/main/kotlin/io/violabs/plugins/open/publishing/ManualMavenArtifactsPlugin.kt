package io.violabs.plugins.open.publishing

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

class ManualMavenArtifactsPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        var overrideDokkaShow: Boolean? = null
        pluginManager.apply("java")
        try {
            pluginManager.apply("org.jetbrains.dokka")
        } catch (e: Exception) {
            logger.warn("Dokka plugin not found. Please ensure it is applied in your build script if you want jars.")
            overrideDokkaShow = false
        }
        pluginManager.apply("maven-publish")

        val extension = project.extensions.create<ManualMavenArtifactsExtension>("pomConfig")

        extension.withDokka = overrideDokkaShow ?: extension.withDokka

        val sourceSets = project.extensions.getByType<SourceSetContainer>()

        // 1) Sources JAR
        val sourcesJar = tasks.register<Jar>("sourcesJar") {
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }

        // 2) Dokka Javadoc JAR
        val dokkaJavadocJar = if (extension.withDokka) {
            tasks.register<Jar>("dokkaJavadocJar") {
                archiveClassifier.set("javadoc")
                from(tasks.named("dokkaJavadoc"))
            }
        } else null

        // 3) Dokka HTML/KDoc JAR
        val dokkaHtmlJar = if (extension.withDokka) {
            tasks.register<Jar>("dokkaHtmlJar") {
                archiveClassifier.set("kdoc")
                from(tasks.named("dokkaHtml"))
            }
        } else null

        // Configure publishing
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])
                    artifact(sourcesJar)
                    dokkaJavadocJar?.apply(::artifact)
                    dokkaHtmlJar?.apply(::artifact)

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

        // 5) Make a single "assembleMavenArtifacts" umbrella task
        tasks.register("assembleMavenArtifacts") {
            dependsOn("jar", sourcesJar, "generatePomFileForMavenPublication")
            if (extension.withDokka) {
                dependsOn(dokkaJavadocJar, dokkaHtmlJar)
            }
            group = "publishing"
            description = "Builds main, sources, javadoc, kdoc jars and the POM."
        }
    }
}