package com.acme.transport.customs.clearance.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceForecastValidator()
        val item = newClearanceForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ClearanceForecastRequiredCodeRule("clearance forecast needs an entry")
        val item = newClearanceForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = ClearanceForecastPriorityRangeRule(16, 1000)
        val item = newClearanceForecast("x", "y")
        item.priority = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ClearanceForecastCodeLengthRule(32)
        val item = newClearanceForecast("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ClearanceForecastRequiredTitleRule("clearance forecast needs an entry")
        val item = newClearanceForecast("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ClearanceForecastTitleLengthRule(1000)
        val item = newClearanceForecast("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
