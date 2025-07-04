package io.violabs.plugins.local.publishing.projectsync

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

abstract class SyncProjectTask : DefaultTask() {
    @get:Input
    abstract var extension: ProjectSyncExtension

    @TaskAction
    fun sync() = project.run {
        val source = extension.syncSource ?: layout.projectDirectory.asFile
        val target = extension.syncTarget ?: throw IllegalArgumentException("syncTarget must be specified.")

        val srcPath = source.toPath()
        val tgtPath = target.toPath()

        logger.lifecycle("Syncing project files from $source to $target")

        source.walkTopDown()
            .filter { it.isFile }
            .forEach { file ->
                // relative path under “open”
                val rel   = srcPath.relativize(file.toPath()).toString()
                // swap “open” → “local” in the path
                val fixed = rel.replace(
                    "${File.separator}open${File.separator}",
                    "${File.separator}local${File.separator}"
                )
                val dest  = tgtPath.resolve(fixed)

                Files.createDirectories(dest.parent)

                // if it’s source code, rewrite its package; otherwise, byte-copy
                if (file.extension in listOf("kt", "java")) {
                    val text = file.readText()
                    // replace “package io.violabs.plugins.open…” → “package io.violabs.plugins.local…”
                    // 1) update package
                    // 2) update any imports (and other references) in one go
                    val updated = text
                        .replace(
                            // only hits the package declaration
                            Regex("""package\s+(.+?)\.open(\..+)"""),
                            "package $1.local$2"
                        )
                        .replace(
                            // hits import lines (and any other code references)
                            Regex("""import\s+(.+?)\.open(\..+)"""),
                            "import $1.local$2"
                        )
                        // (optional) if you have other in-code references use a catch-all:
                        .replace("plugins.open.", "plugins.local.")
                    dest.toFile().writeText(updated)
                }
                else {
                    Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING)
                }

                logger.lifecycle("  → $dest")
            }
    }
}
