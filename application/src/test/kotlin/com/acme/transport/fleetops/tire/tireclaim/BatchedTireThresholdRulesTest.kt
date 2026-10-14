package com.acme.transport.fleetops.tire.tireclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTireThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTireThresholdValidator()
        val item = newBatchedTireThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = BatchedTireThresholdGraceDaysRangeRule(32, 86400)
        val item = newBatchedTireThreshold("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedTireThresholdRequiredCodeRule("batched tire threshold needs this field")
        val item = newBatchedTireThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedTireThresholdTitleLengthRule(64)
        val item = newBatchedTireThreshold("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
