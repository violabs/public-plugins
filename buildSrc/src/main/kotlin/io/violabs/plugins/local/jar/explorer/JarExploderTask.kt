package io.violabs.plugins.local.jar.explorer

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class JarExploderTask : DefaultTask() {

    @TaskAction
    fun explodeJar() {
        project.logger.lifecycle("Exploring jar...")

        val file = project.layout
            .buildDirectory
            .get()
            .asFile
            .resolve("libs/${project.name}.jar")
            .absolutePath

        ProcessBuilder()
            .command("jar", "xf", file)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor()
        project.logger.lifecycle("Jar exploded successfully at: $file")
    }
}