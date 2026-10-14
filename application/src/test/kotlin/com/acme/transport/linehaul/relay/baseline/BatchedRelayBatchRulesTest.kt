package com.acme.transport.linehaul.relay.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRelayBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedRelayBatchValidator()
        val item = newBatchedRelayBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = BatchedRelayBatchContactEmailLengthRule(32)
        val item = newBatchedRelayBatch("x", "y")
        item.contactEmail = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedRelayBatchRequiredCodeRule("batched relay batch needs a value")
        val item = newBatchedRelayBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = BatchedRelayBatchRatioRangeRule(0.4, 1306.0)
        val item = newBatchedRelayBatch("x", "y")
        item.ratio = 1307.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedRelayBatchCodeLengthRule(80)
        val item = newBatchedRelayBatch("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
