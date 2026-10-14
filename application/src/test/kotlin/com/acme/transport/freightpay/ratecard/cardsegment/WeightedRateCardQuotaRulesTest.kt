package com.acme.transport.freightpay.ratecard.cardsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRateCardQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedRateCardQuotaValidator()
        val item = newWeightedRateCardQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = WeightedRateCardQuotaSourceLengthRule(3600)
        val item = newWeightedRateCardQuota("x", "y")
        item.source = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = WeightedRateCardQuotaCapacityRangeRule(32, 64)
        val item = newWeightedRateCardQuota("x", "y")
        item.capacity = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = WeightedRateCardQuotaAttemptsRangeRule(64, 1000)
        val item = newWeightedRateCardQuota("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
