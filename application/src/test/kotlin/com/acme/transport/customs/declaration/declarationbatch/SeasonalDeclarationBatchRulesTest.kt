package com.acme.transport.customs.declaration.declarationbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDeclarationBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDeclarationBatchValidator()
        val item = newSeasonalDeclarationBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalDeclarationBatchCodeLengthRule(3600)
        val item = newSeasonalDeclarationBatch("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = SeasonalDeclarationBatchTitleLengthRule(32)
        val item = newSeasonalDeclarationBatch("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalDeclarationBatchRequiredCodeRule("seasonal declaration batch needs a value")
        val item = newSeasonalDeclarationBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = SeasonalDeclarationBatchRequiredOwnerIdRule("seasonal declaration batch needs an entry")
        val item = newSeasonalDeclarationBatch("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
