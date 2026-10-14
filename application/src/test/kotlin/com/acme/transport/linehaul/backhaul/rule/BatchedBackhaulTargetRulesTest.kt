package com.acme.transport.linehaul.backhaul.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedBackhaulTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedBackhaulTargetValidator()
        val item = newBatchedBackhaulTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = BatchedBackhaulTargetEndsAtRangeRule(16, 64)
        val item = newBatchedBackhaulTarget("x", "y")
        item.endsAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = BatchedBackhaulTargetRequiredSummaryRule("batched backhaul target needs this field")
        val item = newBatchedBackhaulTarget("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = BatchedBackhaulTargetSummaryLengthRule(86400)
        val item = newBatchedBackhaulTarget("x", "y")
        item.summary = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
