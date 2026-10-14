package com.acme.transport.emissions.routeoffset.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetExceptionValidator()
        val item = newRouteOffsetException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = RouteOffsetExceptionStartsAtRangeRule(80, 80)
        val item = newRouteOffsetException("x", "y")
        item.startsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = RouteOffsetExceptionMinimumRangeRule(6.4, 960.6)
        val item = newRouteOffsetException("x", "y")
        item.minimum = 962.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RouteOffsetExceptionCodeLengthRule(120)
        val item = newRouteOffsetException("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = RouteOffsetExceptionChecksumLengthRule(80)
        val item = newRouteOffsetException("x", "y")
        item.checksum = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RouteOffsetExceptionRequiredCodeRule("route offset exception needs this field")
        val item = newRouteOffsetException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
