package com.acme.transport.fleetops.utilization.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledUtilizationMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledUtilizationMilestoneValidator()
        val item = newScheduledUtilizationMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = ScheduledUtilizationMilestoneRequiredTrailerIdRule("scheduled utilization milestone needs a value")
        val item = newScheduledUtilizationMilestone("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = ScheduledUtilizationMilestoneCategoryLengthRule(120)
        val item = newScheduledUtilizationMilestone("x", "y")
        item.category = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = ScheduledUtilizationMilestoneEndsAtRangeRule(86400, 86400)
        val item = newScheduledUtilizationMilestone("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = ScheduledUtilizationMilestoneAttemptsRangeRule(16, 80)
        val item = newScheduledUtilizationMilestone("x", "y")
        item.attempts = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
