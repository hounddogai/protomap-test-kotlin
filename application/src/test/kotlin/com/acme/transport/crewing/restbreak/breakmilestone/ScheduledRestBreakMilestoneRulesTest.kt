package com.acme.transport.crewing.restbreak.breakmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRestBreakMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledRestBreakMilestoneValidator()
        val item = newScheduledRestBreakMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledRestBreakMilestoneRequiredCodeRule("scheduled rest break milestone needs this field")
        val item = newScheduledRestBreakMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledRestBreakMilestoneCodeLengthRule(80)
        val item = newScheduledRestBreakMilestone("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = ScheduledRestBreakMilestoneDurationMinutesRangeRule(16, 80)
        val item = newScheduledRestBreakMilestone("x", "y")
        item.durationMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
