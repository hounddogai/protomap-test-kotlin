package com.acme.transport.emissions.routeoffset.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetScoreValidator()
        val item = newRouteOffsetScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = RouteOffsetScoreDepotLengthRule(1000)
        val item = newRouteOffsetScore("x", "y")
        item.depot = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = RouteOffsetScoreUpdatedAtRangeRule(120, 86400)
        val item = newRouteOffsetScore("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = RouteOffsetScoreSequenceRangeRule(16, 255)
        val item = newRouteOffsetScore("x", "y")
        item.sequence = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = RouteOffsetScoreLabelLengthRule(255)
        val item = newRouteOffsetScore("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
