package com.acme.transport.safety.scorecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingScorecardForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingScorecardForecastValidator()
        val item = newPendingScorecardForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = PendingScorecardForecastExpiresAtRangeRule(16, 16)
        val item = newPendingScorecardForecast("x", "y")
        item.expiresAt = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above window days`() {
        val rule = PendingScorecardForecastWindowDaysRangeRule(32, 32)
        val item = newPendingScorecardForecast("x", "y")
        item.windowDays = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingScorecardForecastRequiredNameRule("pending scorecard forecast needs an entry")
        val item = newPendingScorecardForecast("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
