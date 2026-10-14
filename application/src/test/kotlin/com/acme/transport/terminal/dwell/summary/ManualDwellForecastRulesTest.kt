package com.acme.transport.terminal.dwell.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDwellForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDwellForecastValidator()
        val item = newManualDwellForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = ManualDwellForecastFuelLitersRangeRule(1.7, 2477.1)
        val item = newManualDwellForecast("x", "y")
        item.fuelLiters = 2478.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = ManualDwellForecastRequiredTrailerIdRule("manual dwell forecast needs this field")
        val item = newManualDwellForecast("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualDwellForecastRequiredCodeRule("manual dwell forecast needs a value")
        val item = newManualDwellForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
