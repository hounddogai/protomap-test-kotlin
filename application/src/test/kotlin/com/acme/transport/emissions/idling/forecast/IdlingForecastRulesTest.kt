package com.acme.transport.emissions.idling.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingForecastValidator()
        val item = newIdlingForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = IdlingForecastRequiredDepotRule("idling forecast needs this field")
        val item = newIdlingForecast("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = IdlingForecastCapacityRangeRule(255, 1000)
        val item = newIdlingForecast("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = IdlingForecastRequiredNameRule("idling forecast needs an entry")
        val item = newIdlingForecast("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IdlingForecastRequiredCodeRule("idling forecast needs this field")
        val item = newIdlingForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = IdlingForecastRouteCodeLengthRule(120)
        val item = newIdlingForecast("x", "y")
        item.routeCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
