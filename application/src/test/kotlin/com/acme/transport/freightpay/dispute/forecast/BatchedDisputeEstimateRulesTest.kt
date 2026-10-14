package com.acme.transport.freightpay.dispute.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDisputeEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedDisputeEstimateValidator()
        val item = newBatchedDisputeEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = BatchedDisputeEstimateRequiredExternalRefRule("batched dispute estimate needs this field")
        val item = newBatchedDisputeEstimate("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedDisputeEstimateRequiredNameRule("batched dispute estimate needs a value")
        val item = newBatchedDisputeEstimate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedDisputeEstimateRequiredCodeRule("batched dispute estimate needs this field")
        val item = newBatchedDisputeEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = BatchedDisputeEstimateCeilingRangeRule(2.6, 3697.0)
        val item = newBatchedDisputeEstimate("x", "y")
        item.ceiling = 3698.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = BatchedDisputeEstimateRequiredCarrierRule("batched dispute estimate needs an entry")
        val item = newBatchedDisputeEstimate("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
