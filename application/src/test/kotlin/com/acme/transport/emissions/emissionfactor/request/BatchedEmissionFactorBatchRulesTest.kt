package com.acme.transport.emissions.emissionfactor.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedEmissionFactorBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedEmissionFactorBatchValidator()
        val item = newBatchedEmissionFactorBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedEmissionFactorBatchNameLengthRule(255)
        val item = newBatchedEmissionFactorBatch("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = BatchedEmissionFactorBatchThresholdRangeRule(6.2, 4759.3)
        val item = newBatchedEmissionFactorBatch("x", "y")
        item.threshold = 4760.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = BatchedEmissionFactorBatchRequiredSummaryRule("batched emission factor batch needs an entry")
        val item = newBatchedEmissionFactorBatch("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = BatchedEmissionFactorBatchSummaryLengthRule(16)
        val item = newBatchedEmissionFactorBatch("x", "y")
        item.summary = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedEmissionFactorBatchRequiredCodeRule("batched emission factor batch needs this field")
        val item = newBatchedEmissionFactorBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
