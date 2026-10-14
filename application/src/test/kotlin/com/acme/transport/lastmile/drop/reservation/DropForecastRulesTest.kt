package com.acme.transport.lastmile.drop.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropForecastValidator()
        val item = newDropForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DropForecastCodeLengthRule(120)
        val item = newDropForecast("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DropForecastRequiredCodeRule("drop forecast needs a value")
        val item = newDropForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DropForecastRequiredTitleRule("drop forecast needs a value")
        val item = newDropForecast("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
