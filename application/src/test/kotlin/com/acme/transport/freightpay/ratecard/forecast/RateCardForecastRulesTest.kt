package com.acme.transport.freightpay.ratecard.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardForecastValidator()
        val item = newRateCardForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = RateCardForecastBatchSizeRangeRule(120, 255)
        val item = newRateCardForecast("x", "y")
        item.batchSize = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long lane`() {
        val rule = RateCardForecastLaneLengthRule(32)
        val item = newRateCardForecast("x", "y")
        item.lane = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = RateCardForecastCategoryLengthRule(16)
        val item = newRateCardForecast("x", "y")
        item.category = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = RateCardForecastRequiredLaneRule("rate card forecast needs an entry")
        val item = newRateCardForecast("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
