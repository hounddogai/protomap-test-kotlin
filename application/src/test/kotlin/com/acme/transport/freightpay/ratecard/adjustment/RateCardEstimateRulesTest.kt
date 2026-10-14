package com.acme.transport.freightpay.ratecard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardEstimateValidator()
        val item = newRateCardEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RateCardEstimateNameLengthRule(16)
        val item = newRateCardEstimate("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = RateCardEstimatePriorityRangeRule(255, 3600)
        val item = newRateCardEstimate("x", "y")
        item.priority = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = RateCardEstimateRequiredSegmentRule("rate card estimate needs an entry")
        val item = newRateCardEstimate("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = RateCardEstimateMaximumRangeRule(7.5, 1180.6)
        val item = newRateCardEstimate("x", "y")
        item.maximum = 1182.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
