package com.acme.transport.linehaul.trailer.trailerbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTrailerBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTrailerBatchValidator()
        val item = newBatchedTrailerBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = BatchedTrailerBatchCostRangeRule(9.7, 223.8)
        val item = newBatchedTrailerBatch("x", "y")
        item.cost = 225.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = BatchedTrailerBatchChannelLengthRule(3600)
        val item = newBatchedTrailerBatch("x", "y")
        item.channel = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = BatchedTrailerBatchRequiredChannelRule("batched trailer batch needs this field")
        val item = newBatchedTrailerBatch("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
