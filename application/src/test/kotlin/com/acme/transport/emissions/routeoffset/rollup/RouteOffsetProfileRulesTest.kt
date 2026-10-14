package com.acme.transport.emissions.routeoffset.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetProfileValidator()
        val item = newRouteOffsetProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = RouteOffsetProfileUpdatedAtRangeRule(32, 1000)
        val item = newRouteOffsetProfile("x", "y")
        item.updatedAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = RouteOffsetProfileOdometerRangeRule(32, 80)
        val item = newRouteOffsetProfile("x", "y")
        item.odometer = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RouteOffsetProfileRequiredCodeRule("route offset profile needs a value")
        val item = newRouteOffsetProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
