//package io.violabs.plugins.open.publishing.mavengenerated
//
//import io.violabs.plugins.open.publishing.mavengenerated.domain.ManualMavenArtifactsExtension
//import io.violabs.plugins.open.publishing.mavengenerated.service.MavenGeneratedArtifactsPublishPluginService
//import org.gradle.api.Project
//import org.gradle.api.publish.PublishingExtension
//import org.gradle.api.publish.maven.MavenPublication
//import org.gradle.jvm.tasks.Jar
//import org.gradle.kotlin.dsl.getByType
//import org.gradle.testfixtures.ProjectBuilder
//import org.junit.jupiter.api.Assertions.assertTrue
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.assertNotNull
//import org.junit.jupiter.api.assertNull
//import org.junit.jupiter.api.io.TempDir
//import java.io.File
//import kotlin.test.assertEquals
//
//class PluginServiceUnitTest {
//  private lateinit var project: Project
//  private lateinit var service: MavenGeneratedArtifactsPublishPluginService
//
//  @BeforeEach
//  fun setup(@TempDir tmp: File) {
//    project = ProjectBuilder.builder()
//      .withName("test-project")
//      .withProjectDir(tmp)
//      .build()
//
//    service = MavenGeneratedArtifactsPublishPluginService()
//    service.apply(project)
//  }
//
//  @Test
//  fun `registers extension with defaults`() {
//    val ext = project.extensions.findByName("mavenGeneratedArtifacts")
//      as? ManualMavenArtifactsExtension
//    assertNotNull(ext)
//    // assert default fields, e.g.
//    assertEquals("test-project", ext.name)
//    assertNull(ext.description)
//  }
//
//  @Test
//  fun `creates sources, javadoc and kdoc jars`() {
//    listOf("sourcesJar" to "sources", "dokkaJavadocJar" to "javadoc", "dokkaHtmlJar" to "kdoc")
//      .forEach { (taskName, classifier) ->
//        val t = project.tasks.getByName(taskName) as Jar
//        assertEquals(classifier, t.archiveClassifier.get())
//      }
//  }
//
//  @Test
//  fun `publishes maven publication with correct artifacts`() {
//    val pub = project.extensions
//      .getByType<PublishingExtension>()
//      .publications
//      .getByName("maven") as MavenPublication
//
//    // It should pull from the “java” component…
//    assertNotNull(project.components.find { it.name == "java" })
//
//    // …and have exactly three added artifacts with the correct names
//    val classifiers = pub.artifacts.map { it.classifier }.toSet()
//    assertEquals(setOf("sources", "javadoc", "kdoc"), classifiers)
//  }
//
//  @Test
//  fun `umbrella and hash tasks wired up`() {
//    val assemble = project.tasks.getByName("assembleMavenArtifacts")
//    assertTrue(assemble.dependsOn.containsAll(listOf("jar", "sourcesJar", "dokkaJavadocJar", "dokkaHtmlJar", "generatePomFileForMavenPublication")))
//  }
//}
