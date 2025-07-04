package io.violabs.plugins.local.publishing.projectsync

import org.apache.commons.lang3.StringUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class ProjectSyncTask : DefaultTask() {
    @get:Input
    abstract var extension: ProjectSyncExtension

    @TaskAction
    fun sync(): Unit = project.run {
        val source = extension.syncSource ?: layout.projectDirectory.asFile
        val target = extension.syncTarget ?: throw IllegalArgumentException("syncTarget must be specified.")

        println(target.exists())

        val sourceByFileName = source.walkTopDown().associateBy { it.name }
        val targetByFileName = target.walkTopDown().associateBy { it.name }

        if (sourceByFileName.keys != targetByFileName.keys) {
            logger.warn("sourcesByFileName: ${sourceByFileName.keys}")
            logger.warn("targetByFileName: ${targetByFileName.keys}")
            val missingFromSource = sourceByFileName.keys - targetByFileName.keys
            val tooManyInTarget = targetByFileName.keys - sourceByFileName.keys
            if (extension.deviation() != null) {
                logger.warn(
                    "Source and target directories do not match. " +
                        "Source files missing: $missingFromSource, " +
                        "Target files added that should not: $tooManyInTarget"
                )
                return@run
            }
            throw IllegalStateException(
                "Source and target directories do not match. " +
                    "Source files missing: $missingFromSource, " +
                    "Target files added that should not: $tooManyInTarget"
            )
        }

        sourceByFileName
            .map { (fileName, sourceFile) -> sourceFile to targetByFileName[fileName]!! }
            .filter { (sourceFile, _) -> sourceFile.isFile }
            .forEach { (sourceFile, targetFile) -> xl(sourceFile, targetFile) }
    }

    fun xl(sourceFile: File, targetFile: File) {
        val sourceFileLines = sourceFile.readLines()
        val targetFileLines = targetFile.readLines()

        sourceFileLines.zip(targetFileLines).forEach { (sourceLine, targetLine) ->
            val sanitizedSourceLine = sourceLine.replace(".open", "")
            val sanitizedTargetLine = targetLine.replace(".local", "")

            logger.debug("=========================")
            logger.debug("SOURCE: $sanitizedSourceLine")
            logger.debug("TARGET: $sanitizedTargetLine")
            logger.debug("=========================")
            if (sanitizedSourceLine != sanitizedTargetLine) {
                val difference = StringUtils.difference(sanitizedSourceLine, sanitizedTargetLine)
                logger.warn("Difference found: ${difference.replace(" ", ".")}")

                if (extension.deviation() == null) {
                    throw IllegalStateException("Source and target files do not match for ${sourceFile.name}.")
                } else {
                    logger.warn("Source and target files do not match for ${sourceFile.name}. " +
                        "This is expected in this project, but please ensure that the files are in sync.")
                }
            }
        }
    }

    fun cleanout(sourceFile: File, targetFile: File) {
        val sanitizedSource = sourceFile.readText().replace(".open", "")
        val sanitizedTarget = targetFile.readText().replace(".local", "")

        if (sanitizedSource != sanitizedTarget) {
            val targetLines = sanitizedTarget.lines()
            sanitizedSource.lines().mapIndexed { index, line ->
                val targetLine = targetLines[index]

                StringUtils.difference(targetLine, line)
            }.forEach { difference ->
                logger.info(difference)
            }

            if (extension.deviation() == null) {
                throw IllegalStateException("Source and target files do not match for ${sourceFile.name}.")
            } else {
                logger.warn("Source and target files do not match for ${sourceFile.name}. " +
                    "This is expected in this project, but please ensure that the files are in sync.")
            }
        }
    }
}