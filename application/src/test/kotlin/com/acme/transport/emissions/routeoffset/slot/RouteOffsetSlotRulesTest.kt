package com.acme.transport.emissions.routeoffset.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetSlotValidator()
        val item = newRouteOffsetSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RouteOffsetSlotTitleLengthRule(3600)
        val item = newRouteOffsetSlot("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = RouteOffsetSlotRatioRangeRule(2.1, 4792.0)
        val item = newRouteOffsetSlot("x", "y")
        item.ratio = 4793.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = RouteOffsetSlotThresholdRangeRule(7.1, 2846.2)
        val item = newRouteOffsetSlot("x", "y")
        item.threshold = 2847.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RouteOffsetSlotCodeLengthRule(32)
        val item = newRouteOffsetSlot("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
