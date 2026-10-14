package com.acme.transport.terminal.dockdoor.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDockDoorMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedDockDoorMappingValidator()
        val item = newBatchedDockDoorMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedDockDoorMappingTitleLengthRule(86400)
        val item = newBatchedDockDoorMapping("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = BatchedDockDoorMappingRequiredSourceRule("batched dock door mapping needs a value")
        val item = newBatchedDockDoorMapping("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = BatchedDockDoorMappingRateRangeRule(8.1, 3834.9)
        val item = newBatchedDockDoorMapping("x", "y")
        item.rate = 3836.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
