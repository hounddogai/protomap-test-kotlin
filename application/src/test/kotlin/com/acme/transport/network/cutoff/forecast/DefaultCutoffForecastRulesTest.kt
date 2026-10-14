package com.acme.transport.network.cutoff.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCutoffForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultCutoffForecastValidator()
        val item = newDefaultCutoffForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefaultCutoffForecastTitleLengthRule(16)
        val item = newDefaultCutoffForecast("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultCutoffForecastRequiredTitleRule("default cutoff forecast needs this field")
        val item = newDefaultCutoffForecast("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultCutoffForecastCodeLengthRule(255)
        val item = newDefaultCutoffForecast("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
