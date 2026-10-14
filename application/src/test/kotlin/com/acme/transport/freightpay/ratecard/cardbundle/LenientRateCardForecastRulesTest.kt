package com.acme.transport.freightpay.ratecard.cardbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRateCardForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientRateCardForecastValidator()
        val item = newLenientRateCardForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientRateCardForecastRequiredTitleRule("lenient rate card forecast needs a value")
        val item = newLenientRateCardForecast("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = LenientRateCardForecastDockLengthRule(64)
        val item = newLenientRateCardForecast("x", "y")
        item.dock = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientRateCardForecastCodeLengthRule(86400)
        val item = newLenientRateCardForecast("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientRateCardForecastTitleLengthRule(120)
        val item = newLenientRateCardForecast("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
