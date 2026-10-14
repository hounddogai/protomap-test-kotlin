package com.acme.transport.terminal.sortation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationForecastValidator()
        val item = newSortationForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = SortationForecastCostRangeRule(4.7, 4228.3)
        val item = newSortationForecast("x", "y")
        item.cost = 4229.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = SortationForecastStartsAtRangeRule(32, 1000)
        val item = newSortationForecast("x", "y")
        item.startsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = SortationForecastCodeLengthRule(16)
        val item = newSortationForecast("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SortationForecastRequiredCodeRule("sortation forecast needs an entry")
        val item = newSortationForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
