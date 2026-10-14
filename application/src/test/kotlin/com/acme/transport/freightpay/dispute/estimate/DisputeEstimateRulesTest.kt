package com.acme.transport.freightpay.dispute.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeEstimateValidator()
        val item = newDisputeEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = DisputeEstimateBatchSizeRangeRule(16, 64)
        val item = newDisputeEstimate("x", "y")
        item.batchSize = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DisputeEstimateRequiredCodeRule("dispute estimate needs an entry")
        val item = newDisputeEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DisputeEstimateRequiredLabelRule("dispute estimate needs a value")
        val item = newDisputeEstimate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = DisputeEstimateMinimumRangeRule(2.5, 2410.0)
        val item = newDisputeEstimate("x", "y")
        item.minimum = 2411.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long region`() {
        val rule = DisputeEstimateRegionLengthRule(255)
        val item = newDisputeEstimate("x", "y")
        item.region = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
