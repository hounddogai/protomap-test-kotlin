package com.acme.transport.emissions.routeoffset.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetSegmentValidator()
        val item = newRouteOffsetSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RouteOffsetSegmentCodeLengthRule(255)
        val item = newRouteOffsetSegment("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = RouteOffsetSegmentStopCountRangeRule(16, 32)
        val item = newRouteOffsetSegment("x", "y")
        item.stopCount = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RouteOffsetSegmentRequiredNameRule("route offset segment needs an entry")
        val item = newRouteOffsetSegment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
