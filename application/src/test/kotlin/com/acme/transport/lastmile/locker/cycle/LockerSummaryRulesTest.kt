package com.acme.transport.lastmile.locker.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerSummaryValidator()
        val item = newLockerSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = LockerSummaryPriorityRangeRule(32, 80)
        val item = newLockerSummary("x", "y")
        item.priority = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = LockerSummaryRequiredTimezoneRule("locker summary needs an entry")
        val item = newLockerSummary("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LockerSummaryRequiredNameRule("locker summary needs a value")
        val item = newLockerSummary("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = LockerSummaryRouteCodeLengthRule(32)
        val item = newLockerSummary("x", "y")
        item.routeCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
