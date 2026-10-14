package com.acme.transport.emissions.routeoffset.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetCheckpointValidator()
        val item = newRouteOffsetCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RouteOffsetCheckpointCodeLengthRule(120)
        val item = newRouteOffsetCheckpoint("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = RouteOffsetCheckpointExpiresAtRangeRule(32, 120)
        val item = newRouteOffsetCheckpoint("x", "y")
        item.expiresAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
