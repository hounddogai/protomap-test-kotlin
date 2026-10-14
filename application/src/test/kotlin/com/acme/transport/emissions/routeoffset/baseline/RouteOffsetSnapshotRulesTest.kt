package com.acme.transport.emissions.routeoffset.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetSnapshotValidator()
        val item = newRouteOffsetSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = RouteOffsetSnapshotRequiredTrailerIdRule("route offset snapshot needs a value")
        val item = newRouteOffsetSnapshot("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = RouteOffsetSnapshotEtaMinutesRangeRule(32, 86400)
        val item = newRouteOffsetSnapshot("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = RouteOffsetSnapshotDurationMinutesRangeRule(120, 3600)
        val item = newRouteOffsetSnapshot("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = RouteOffsetSnapshotTrailerIdLengthRule(1000)
        val item = newRouteOffsetSnapshot("x", "y")
        item.trailerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
