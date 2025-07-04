package io.violabs.plugins.local.publishing.mavengenerated.service

import io.violabs.plugins.local.publishing.mavengenerated.domain.ManualMavenArtifactsExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import java.io.File
import java.security.MessageDigest


open class MavenGeneratedArtifactsPublishPluginService : BuildService<MavenGeneratedArtifactsPublishPluginService.Params>  {
    interface Params : BuildServiceParameters

    override fun getParameters(): Params = object : Params {}

    fun apply(project: Project): Project = project.run {
        pluginManager.apply("java")
        pluginManager.apply("maven-publish")

        val extension = project.extensions.create<ManualMavenArtifactsExtension>("mavenGeneratedArtifacts")

        project.afterEvaluate {
            val sourcesJar: TaskProvider<Jar> = createSourcesJar()

            val (dokkaJavadocJar, dokkaHtmlJar) = createDokkaTasksIfPluginEnabled(extension)

            val jarTasks: MutableList<TaskProvider<Jar>> = nonNullMutableList(sourcesJar, dokkaJavadocJar, dokkaHtmlJar)

            configurePom(extension, artifactProviders = jarTasks)

            val generateHashTask = addGenerateHashesTask()

            addAssembleMavenArtifactsTask(dependsOn = jarTasks, finalizedBy = generateHashTask)
        }

        return project
    }

    private fun Project.createSourcesJar(): TaskProvider<Jar> {
        val sourceSets = extensions.getByType<SourceSetContainer>()

        return tasks.register<Jar>("sourcesJar") {
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }
    }

    private fun Project.createDokkaTasksIfPluginEnabled(
        extension: ManualMavenArtifactsExtension
    ): Pair<TaskProvider<Jar>?, TaskProvider<Jar>?> {
        var dokkaJavadocJar: TaskProvider<Jar>? = null
        var dokkaHtmlJar: TaskProvider<Jar>?  = null

        if (extension.withDokka) {
            pluginManager.apply("org.jetbrains.dokka")
            dokkaJavadocJar = tasks.register<Jar>("dokkaJavadocJar") {
                archiveClassifier.set("javadoc")
                from(tasks.named("dokkaJavadoc"))
            }

            dokkaHtmlJar = tasks.register<Jar>("dokkaHtmlJar") {
                archiveClassifier.set("kdoc")
                from(tasks.named("dokkaHtml"))
            }
        }

        return dokkaJavadocJar to dokkaHtmlJar
    }

    private fun Project.configurePom(
        extension: ManualMavenArtifactsExtension,
        artifactProviders: List<TaskProvider<Jar>>
    ) {
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>(extension.publicationName) {
                    from(components["java"])
                    artifactProviders.forEach { artifact ->
                        artifact(artifact)
                    }

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
                            val connectionLocation = scm?.connection
                                ?: throw IllegalArgumentException("SCM connection is required")
                            val developerConnectionLocation = scm.developerConnection ?: connection
                            connection.set("scm:git:git://$connectionLocation")
                            developerConnection.set("scm:git:ssh://$developerConnectionLocation")
                            url.set(scm.url ?: extension.websiteUrl)
                        }
                    }
                }
            }
        }
    }

    fun <T> nonNullMutableList(vararg items: T?): MutableList<T> = sequenceOf(*items)
        .filterNotNull()
        .toMutableList()

    fun Project.addGenerateHashesTask(): TaskProvider<Task> {
        return tasks.register("generateHashes") {
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
    }

    fun Project.addAssembleMavenArtifactsTask(
        dependsOn: MutableList<TaskProvider<Jar>>,
        finalizedBy: TaskProvider<Task>
    ) {
        tasks.register("assembleMavenArtifacts") {
            group = "distribution"
            description = "Builds main, sources, javadoc, kdoc jars and the POM."
            dependsOn("jar", "generatePomFileForMavenPublication")
            dependsOn(*dependsOn.toTypedArray())
            finalizedBy(finalizedBy)
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