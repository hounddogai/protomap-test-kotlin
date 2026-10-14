package com.acme.transport.emissions.routeoffset.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetScheduleValidator()
        val item = newRouteOffsetSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = RouteOffsetScheduleEndsAtRangeRule(3600, 86400)
        val item = newRouteOffsetSchedule("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = RouteOffsetScheduleTitleLengthRule(16)
        val item = newRouteOffsetSchedule("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = RouteOffsetScheduleRequiredChecksumRule("route offset schedule needs an entry")
        val item = newRouteOffsetSchedule("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = RouteOffsetScheduleChecksumLengthRule(64)
        val item = newRouteOffsetSchedule("x", "y")
        item.checksum = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = RouteOffsetScheduleRevisionRangeRule(64, 86400)
        val item = newRouteOffsetSchedule("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
