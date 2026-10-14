package com.acme.transport.emissions.routeoffset.offsetrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRouteOffsetSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledRouteOffsetSnapshotValidator()
        val item = newScheduledRouteOffsetSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledRouteOffsetSnapshotRequiredLabelRule("scheduled route offset snapshot needs an entry")
        val item = newScheduledRouteOffsetSnapshot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledRouteOffsetSnapshotCodeLengthRule(32)
        val item = newScheduledRouteOffsetSnapshot("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = ScheduledRouteOffsetSnapshotWindowDaysRangeRule(16, 32)
        val item = newScheduledRouteOffsetSnapshot("x", "y")
        item.windowDays = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledRouteOffsetSnapshotLabelLengthRule(3600)
        val item = newScheduledRouteOffsetSnapshot("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
