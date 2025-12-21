package io.violabs.plugins.open.ai.claude.claudecode.skillresolver.domain

import io.violabs.test.core.UnitTest
import org.junit.jupiter.api.Test

class SkillResolutionResultTest : UnitTest() {

    @Test
    fun `Success should calculate totalFiles correctly`() {
        val result = SkillResolutionResult.Success(
            resolvedSkills = listOf(
                ResolvedSkill("pdf", listOf(
                    SkillFile("pdf", "file1.md", "content1", "url1"),
                    SkillFile("pdf", "file2.md", "content2", "url2")
                )),
                ResolvedSkill("docx", listOf(
                    SkillFile("docx", "file1.md", "content3", "url3")
                ))
            )
        )

        result.totalFiles eq 3
    }

    @Test
    fun `Success should calculate skillCount correctly`() {
        val result = SkillResolutionResult.Success(
            resolvedSkills = listOf(
                ResolvedSkill("pdf", emptyList()),
                ResolvedSkill("docx", emptyList()),
                ResolvedSkill("xlsx", emptyList())
            )
        )

        result.skillCount eq 3
    }

    @Test
    fun `Success should handle empty resolvedSkills`() {
        val result = SkillResolutionResult.Success(
            resolvedSkills = emptyList()
        )

        result.totalFiles eq 0
        result.skillCount eq 0
    }

    @Test
    fun `Success should store warnings`() {
        val result = SkillResolutionResult.Success(
            resolvedSkills = emptyList(),
            warnings = listOf("warning1", "warning2")
        )

        result.warnings.size eq 2
        result.warnings[0] eq "warning1"
    }

    @Test
    fun `Failure should store errors`() {
        val result = SkillResolutionResult.Failure(
            errors = listOf("error1", "error2")
        )

        result.errors.size eq 2
        result.errors[0] eq "error1"
    }

    @Test
    fun `Failure should store partial results`() {
        val partialSkill = ResolvedSkill("pdf", listOf(
            SkillFile("pdf", "file.md", "content", "url")
        ))

        val result = SkillResolutionResult.Failure(
            errors = listOf("error"),
            partialResults = listOf(partialSkill)
        )

        result.partialResults.size eq 1
        result.partialResults[0].name eq "pdf"
    }

    @Test
    fun `ResolvedSkill should calculate fileCount correctly`() {
        val skill = ResolvedSkill("pdf", listOf(
            SkillFile("pdf", "file1.md", "content1", "url1"),
            SkillFile("pdf", "file2.md", "content2", "url2"),
            SkillFile("pdf", "file3.md", "content3", "url3")
        ))

        skill.fileCount eq 3
    }
}
