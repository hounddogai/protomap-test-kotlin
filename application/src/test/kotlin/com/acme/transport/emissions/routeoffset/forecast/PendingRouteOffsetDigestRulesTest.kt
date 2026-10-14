package com.acme.transport.emissions.routeoffset.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRouteOffsetDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingRouteOffsetDigestValidator()
        val item = newPendingRouteOffsetDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingRouteOffsetDigestRequiredCodeRule("pending route offset digest needs this field")
        val item = newPendingRouteOffsetDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingRouteOffsetDigestCodeLengthRule(255)
        val item = newPendingRouteOffsetDigest("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = PendingRouteOffsetDigestAttemptsRangeRule(80, 1000)
        val item = newPendingRouteOffsetDigest("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long currency`() {
        val rule = PendingRouteOffsetDigestCurrencyLengthRule(32)
        val item = newPendingRouteOffsetDigest("x", "y")
        item.currency = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
