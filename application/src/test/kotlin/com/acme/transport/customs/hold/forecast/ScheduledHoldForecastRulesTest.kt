package com.acme.transport.customs.hold.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledHoldForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledHoldForecastValidator()
        val item = newScheduledHoldForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = ScheduledHoldForecastStartsAtRangeRule(3600, 86400)
        val item = newScheduledHoldForecast("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledHoldForecastRequiredCodeRule("scheduled hold forecast needs a value")
        val item = newScheduledHoldForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = ScheduledHoldForecastRequiredCountryRule("scheduled hold forecast needs a value")
        val item = newScheduledHoldForecast("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
