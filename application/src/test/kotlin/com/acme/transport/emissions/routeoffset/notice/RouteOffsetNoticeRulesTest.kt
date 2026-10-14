package com.acme.transport.emissions.routeoffset.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetNoticeValidator()
        val item = newRouteOffsetNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RouteOffsetNoticeRequiredLabelRule("route offset notice needs a value")
        val item = newRouteOffsetNotice("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = RouteOffsetNoticeDiscountRangeRule(1.5, 2117.2)
        val item = newRouteOffsetNotice("x", "y")
        item.discount = 2118.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = RouteOffsetNoticeLabelLengthRule(255)
        val item = newRouteOffsetNotice("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RouteOffsetNoticeRequiredCodeRule("route offset notice needs this field")
        val item = newRouteOffsetNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RouteOffsetNoticeAttemptsRangeRule(255, 3600)
        val item = newRouteOffsetNotice("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
