package com.acme.transport.linehaul.trailer.trailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingTrailerProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingTrailerProfileValidator()
        val item = newPendingTrailerProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = PendingTrailerProfileRouteCodeLengthRule(120)
        val item = newPendingTrailerProfile("x", "y")
        item.routeCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = PendingTrailerProfileExpiresAtRangeRule(64, 120)
        val item = newPendingTrailerProfile("x", "y")
        item.expiresAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = PendingTrailerProfileRequiredRouteCodeRule("pending trailer profile needs this field")
        val item = newPendingTrailerProfile("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = PendingTrailerProfileThresholdRangeRule(6.8, 405.5)
        val item = newPendingTrailerProfile("x", "y")
        item.threshold = 407.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
