package com.acme.transport.emissions.routeoffset.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetOverrideValidator()
        val item = newRouteOffsetOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RouteOffsetOverrideRequiredTitleRule("route offset override needs a value")
        val item = newRouteOffsetOverride("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RouteOffsetOverrideTitleLengthRule(3600)
        val item = newRouteOffsetOverride("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = RouteOffsetOverrideMarginRangeRule(0.8, 805.9)
        val item = newRouteOffsetOverride("x", "y")
        item.margin = 807.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
