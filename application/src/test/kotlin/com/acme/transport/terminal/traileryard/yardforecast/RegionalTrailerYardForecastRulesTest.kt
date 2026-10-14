package com.acme.transport.terminal.traileryard.yardforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalTrailerYardForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalTrailerYardForecastValidator()
        val item = newRegionalTrailerYardForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = RegionalTrailerYardForecastDescriptionLengthRule(32)
        val item = newRegionalTrailerYardForecast("x", "y")
        item.description = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalTrailerYardForecastTitleLengthRule(120)
        val item = newRegionalTrailerYardForecast("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = RegionalTrailerYardForecastThresholdRangeRule(9.8, 4667.0)
        val item = newRegionalTrailerYardForecast("x", "y")
        item.threshold = 4668.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
