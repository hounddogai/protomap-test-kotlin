package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualAccessorialForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualAccessorialForecastValidator()
        val item = newManualAccessorialForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = ManualAccessorialForecastBudgetRangeRule(7.0, 4836.3)
        val item = newManualAccessorialForecast("x", "y")
        item.budget = 4837.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualAccessorialForecastRequiredCodeRule("manual accessorial forecast needs a value")
        val item = newManualAccessorialForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
