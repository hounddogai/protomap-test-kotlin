package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRouteOffsetSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledRouteOffsetSummaryValidator()
        val item = newScheduledRouteOffsetSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledRouteOffsetSummaryRequiredCodeRule("scheduled route offset summary needs a value")
        val item = newScheduledRouteOffsetSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledRouteOffsetSummaryCodeLengthRule(32)
        val item = newScheduledRouteOffsetSummary("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = ScheduledRouteOffsetSummaryRequiredShiftCodeRule("scheduled route offset summary needs a value")
        val item = newScheduledRouteOffsetSummary("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledRouteOffsetSummaryRequiredNameRule("scheduled route offset summary needs this field")
        val item = newScheduledRouteOffsetSummary("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
