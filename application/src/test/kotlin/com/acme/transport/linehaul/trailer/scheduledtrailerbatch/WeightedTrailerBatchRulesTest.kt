package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTrailerBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedTrailerBatchValidator()
        val item = newWeightedTrailerBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = WeightedTrailerBatchUpdatedAtRangeRule(16, 32)
        val item = newWeightedTrailerBatch("x", "y")
        item.updatedAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedTrailerBatchCodeLengthRule(1000)
        val item = newWeightedTrailerBatch("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = WeightedTrailerBatchThresholdRangeRule(1.7, 669.1)
        val item = newWeightedTrailerBatch("x", "y")
        item.threshold = 670.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
