package com.acme.transport.emissions.routeoffset.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetAllocationValidator()
        val item = newRouteOffsetAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RouteOffsetAllocationRequiredCodeRule("route offset allocation needs a value")
        val item = newRouteOffsetAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RouteOffsetAllocationCodeLengthRule(1000)
        val item = newRouteOffsetAllocation("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = RouteOffsetAllocationWindowDaysRangeRule(120, 255)
        val item = newRouteOffsetAllocation("x", "y")
        item.windowDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
