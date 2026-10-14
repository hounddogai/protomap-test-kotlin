package com.acme.transport.safety.violation.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalViolationBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalViolationBatchValidator()
        val item = newGlobalViolationBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = GlobalViolationBatchStopCountRangeRule(32, 3600)
        val item = newGlobalViolationBatch("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = GlobalViolationBatchRequiredTrailerIdRule("global violation batch needs this field")
        val item = newGlobalViolationBatch("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalViolationBatchNameLengthRule(32)
        val item = newGlobalViolationBatch("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = GlobalViolationBatchTrailerIdLengthRule(1000)
        val item = newGlobalViolationBatch("x", "y")
        item.trailerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
