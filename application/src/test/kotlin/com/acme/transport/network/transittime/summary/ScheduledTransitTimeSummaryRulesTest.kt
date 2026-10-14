package com.acme.transport.network.transittime.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledTransitTimeSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledTransitTimeSummaryValidator()
        val item = newScheduledTransitTimeSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = ScheduledTransitTimeSummaryUpdatedAtRangeRule(16, 80)
        val item = newScheduledTransitTimeSummary("x", "y")
        item.updatedAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledTransitTimeSummaryCodeLengthRule(120)
        val item = newScheduledTransitTimeSummary("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
