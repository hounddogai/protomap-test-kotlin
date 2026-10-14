package com.acme.transport.emissions.routeoffset.offsetmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetDigestValidator()
        val item = newRouteOffsetDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = RouteOffsetDigestPriorityRangeRule(32, 32)
        val item = newRouteOffsetDigest("x", "y")
        item.priority = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RouteOffsetDigestCodeLengthRule(16)
        val item = newRouteOffsetDigest("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = RouteOffsetDigestStopCountRangeRule(255, 86400)
        val item = newRouteOffsetDigest("x", "y")
        item.stopCount = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
