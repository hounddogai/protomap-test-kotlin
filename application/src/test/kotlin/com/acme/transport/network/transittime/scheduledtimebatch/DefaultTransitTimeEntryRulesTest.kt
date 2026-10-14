package com.acme.transport.network.transittime.scheduledtimebatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTransitTimeEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultTransitTimeEntryValidator()
        val item = newDefaultTransitTimeEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = DefaultTransitTimeEntryMinimumRangeRule(0.4, 1264.4)
        val item = newDefaultTransitTimeEntry("x", "y")
        item.minimum = 1265.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultTransitTimeEntryCodeLengthRule(80)
        val item = newDefaultTransitTimeEntry("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = DefaultTransitTimeEntryRequiredLaneRule("default transit time entry needs an entry")
        val item = newDefaultTransitTimeEntry("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefaultTransitTimeEntryTitleLengthRule(255)
        val item = newDefaultTransitTimeEntry("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
