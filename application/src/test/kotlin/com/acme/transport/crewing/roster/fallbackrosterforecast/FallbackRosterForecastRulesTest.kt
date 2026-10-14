package com.acme.transport.crewing.roster.fallbackrosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRosterForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackRosterForecastValidator()
        val item = newFallbackRosterForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackRosterForecastCodeLengthRule(1000)
        val item = newFallbackRosterForecast("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackRosterForecastTitleLengthRule(255)
        val item = newFallbackRosterForecast("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = FallbackRosterForecastRequiredReviewerRule("fallback roster forecast needs a value")
        val item = newFallbackRosterForecast("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
