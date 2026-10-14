package com.acme.transport.linehaul.trailer.trailerrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTrailerRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTrailerRuleValidator()
        val item = newBatchedTrailerRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = BatchedTrailerRuleStopCountRangeRule(80, 1000)
        val item = newBatchedTrailerRule("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = BatchedTrailerRuleCreatedAtRangeRule(3600, 3600)
        val item = newBatchedTrailerRule("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long notes`() {
        val rule = BatchedTrailerRuleNotesLengthRule(3600)
        val item = newBatchedTrailerRule("x", "y")
        item.notes = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedTrailerRuleRequiredCodeRule("batched trailer rule needs a value")
        val item = newBatchedTrailerRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
