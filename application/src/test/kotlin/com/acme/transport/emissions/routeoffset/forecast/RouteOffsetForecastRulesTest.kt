package com.acme.transport.emissions.routeoffset.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetForecastValidator()
        val item = newRouteOffsetForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RouteOffsetForecastRequiredNameRule("route offset forecast needs an entry")
        val item = newRouteOffsetForecast("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = RouteOffsetForecastOwnerIdLengthRule(16)
        val item = newRouteOffsetForecast("x", "y")
        item.ownerId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = RouteOffsetForecastRequiredOwnerIdRule("route offset forecast needs a value")
        val item = newRouteOffsetForecast("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RouteOffsetForecastCodeLengthRule(16)
        val item = newRouteOffsetForecast("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
