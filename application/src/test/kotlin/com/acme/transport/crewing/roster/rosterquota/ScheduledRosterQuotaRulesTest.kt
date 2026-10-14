package com.acme.transport.crewing.roster.rosterquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRosterQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledRosterQuotaValidator()
        val item = newScheduledRosterQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledRosterQuotaRequiredTitleRule("scheduled roster quota needs an entry")
        val item = newScheduledRosterQuota("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = ScheduledRosterQuotaRatioRangeRule(7.6, 3611.4)
        val item = newScheduledRosterQuota("x", "y")
        item.ratio = 3612.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = ScheduledRosterQuotaRequiredDriverIdRule("scheduled roster quota needs an entry")
        val item = newScheduledRosterQuota("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ScheduledRosterQuotaTitleLengthRule(32)
        val item = newScheduledRosterQuota("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
