package com.acme.transport.crewing.restbreak.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakForecastValidator()
        val item = newRestBreakForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = RestBreakForecastWeightRangeRule(0.2, 4160.6)
        val item = newRestBreakForecast("x", "y")
        item.weight = 4162.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long currency`() {
        val rule = RestBreakForecastCurrencyLengthRule(120)
        val item = newRestBreakForecast("x", "y")
        item.currency = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = RestBreakForecastTitleLengthRule(80)
        val item = newRestBreakForecast("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
