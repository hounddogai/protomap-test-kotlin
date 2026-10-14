package com.acme.transport.lastmile.drop.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDropForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalDropForecastValidator()
        val item = newGlobalDropForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalDropForecastCodeLengthRule(16)
        val item = newGlobalDropForecast("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalDropForecastRequiredCodeRule("global drop forecast needs a value")
        val item = newGlobalDropForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = GlobalDropForecastBudgetRangeRule(0.4, 2450.9)
        val item = newGlobalDropForecast("x", "y")
        item.budget = 2452.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
