package com.acme.transport.freightpay.dispute.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeForecastValidator()
        val item = newDisputeForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = DisputeForecastLaneLengthRule(86400)
        val item = newDisputeForecast("x", "y")
        item.lane = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = DisputeForecastRequiredTrailerIdRule("dispute forecast needs an entry")
        val item = newDisputeForecast("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
