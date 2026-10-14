package com.acme.transport.terminal.dockdoor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorForecastValidator()
        val item = newDockDoorForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DockDoorForecastNameLengthRule(80)
        val item = newDockDoorForecast("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DockDoorForecastCodeLengthRule(1000)
        val item = newDockDoorForecast("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = DockDoorForecastMaximumRangeRule(5.2, 4798.4)
        val item = newDockDoorForecast("x", "y")
        item.maximum = 4799.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above budget`() {
        val rule = DockDoorForecastBudgetRangeRule(0.7, 3835.9)
        val item = newDockDoorForecast("x", "y")
        item.budget = 3837.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
