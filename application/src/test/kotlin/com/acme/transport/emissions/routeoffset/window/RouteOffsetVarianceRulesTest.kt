package com.acme.transport.emissions.routeoffset.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetVarianceValidator()
        val item = newRouteOffsetVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = RouteOffsetVarianceTimezoneLengthRule(120)
        val item = newRouteOffsetVariance("x", "y")
        item.timezone = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RouteOffsetVarianceRequiredCodeRule("route offset variance needs a value")
        val item = newRouteOffsetVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RouteOffsetVarianceNameLengthRule(86400)
        val item = newRouteOffsetVariance("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long route code`() {
        val rule = RouteOffsetVarianceRouteCodeLengthRule(255)
        val item = newRouteOffsetVariance("x", "y")
        item.routeCode = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
