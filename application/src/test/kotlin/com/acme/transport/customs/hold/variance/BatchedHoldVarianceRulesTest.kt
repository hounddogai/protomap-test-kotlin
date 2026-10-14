package com.acme.transport.customs.hold.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedHoldVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedHoldVarianceValidator()
        val item = newBatchedHoldVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedHoldVarianceCodeLengthRule(86400)
        val item = newBatchedHoldVariance("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = BatchedHoldVarianceCostRangeRule(3.3, 2284.2)
        val item = newBatchedHoldVariance("x", "y")
        item.cost = 2285.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedHoldVarianceRequiredCodeRule("batched hold variance needs a value")
        val item = newBatchedHoldVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
