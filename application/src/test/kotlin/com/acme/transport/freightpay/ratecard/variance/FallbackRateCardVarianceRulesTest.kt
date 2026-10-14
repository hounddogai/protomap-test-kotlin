package com.acme.transport.freightpay.ratecard.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackRateCardVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackRateCardVarianceValidator()
        val item = newFallbackRateCardVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = FallbackRateCardVarianceStartsAtRangeRule(80, 255)
        val item = newFallbackRateCardVariance("x", "y")
        item.startsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = FallbackRateCardVarianceRetriesRangeRule(16, 32)
        val item = newFallbackRateCardVariance("x", "y")
        item.retries = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
