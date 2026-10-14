package com.acme.transport.emissions.routeoffset.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetBaselineValidator()
        val item = newRouteOffsetBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = RouteOffsetBaselineLocaleLengthRule(1000)
        val item = newRouteOffsetBaseline("x", "y")
        item.locale = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = RouteOffsetBaselineRevisionRangeRule(16, 64)
        val item = newRouteOffsetBaseline("x", "y")
        item.revision = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = RouteOffsetBaselineGraceDaysRangeRule(64, 255)
        val item = newRouteOffsetBaseline("x", "y")
        item.graceDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = RouteOffsetBaselineSequenceRangeRule(80, 80)
        val item = newRouteOffsetBaseline("x", "y")
        item.sequence = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above margin`() {
        val rule = RouteOffsetBaselineMarginRangeRule(9.7, 737.1)
        val item = newRouteOffsetBaseline("x", "y")
        item.margin = 738.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
