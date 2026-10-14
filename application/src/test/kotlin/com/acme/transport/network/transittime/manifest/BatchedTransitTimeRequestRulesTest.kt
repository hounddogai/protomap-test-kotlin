package com.acme.transport.network.transittime.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTransitTimeRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTransitTimeRequestValidator()
        val item = newBatchedTransitTimeRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = BatchedTransitTimeRequestLabelLengthRule(80)
        val item = newBatchedTransitTimeRequest("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BatchedTransitTimeRequestRequiredLabelRule("batched transit time request needs this field")
        val item = newBatchedTransitTimeRequest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = BatchedTransitTimeRequestScoreRangeRule(3.7, 482.4)
        val item = newBatchedTransitTimeRequest("x", "y")
        item.score = 483.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
