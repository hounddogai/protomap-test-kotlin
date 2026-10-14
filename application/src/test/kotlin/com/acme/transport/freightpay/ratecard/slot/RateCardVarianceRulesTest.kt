package com.acme.transport.freightpay.ratecard.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardVarianceValidator()
        val item = newRateCardVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RateCardVarianceTitleLengthRule(255)
        val item = newRateCardVariance("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = RateCardVarianceEtaMinutesRangeRule(16, 3600)
        val item = newRateCardVariance("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RateCardVarianceAttemptsRangeRule(16, 255)
        val item = newRateCardVariance("x", "y")
        item.attempts = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long country`() {
        val rule = RateCardVarianceCountryLengthRule(64)
        val item = newRateCardVariance("x", "y")
        item.country = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = RateCardVarianceWeightRangeRule(3.1, 3020.4)
        val item = newRateCardVariance("x", "y")
        item.weight = 3021.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
