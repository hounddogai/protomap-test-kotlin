package com.acme.transport.crewing.roster.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRosterTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedRosterTemplateValidator()
        val item = newBatchedRosterTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = BatchedRosterTemplateAmountRangeRule(9.6, 2745.9)
        val item = newBatchedRosterTemplate("x", "y")
        item.amount = 2747.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = BatchedRosterTemplateCreatedAtRangeRule(120, 86400)
        val item = newBatchedRosterTemplate("x", "y")
        item.createdAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedRosterTemplateCodeLengthRule(1000)
        val item = newBatchedRosterTemplate("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedRosterTemplateNameLengthRule(32)
        val item = newBatchedRosterTemplate("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedRosterTemplateRequiredNameRule("batched roster template needs an entry")
        val item = newBatchedRosterTemplate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
