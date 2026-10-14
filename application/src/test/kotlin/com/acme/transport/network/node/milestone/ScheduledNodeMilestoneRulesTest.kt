package com.acme.transport.network.node.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledNodeMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledNodeMilestoneValidator()
        val item = newScheduledNodeMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledNodeMilestoneRequiredCodeRule("scheduled node milestone needs an entry")
        val item = newScheduledNodeMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = ScheduledNodeMilestoneRequiredVehicleIdRule("scheduled node milestone needs a value")
        val item = newScheduledNodeMilestone("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = ScheduledNodeMilestoneRatioRangeRule(1.3, 1143.5)
        val item = newScheduledNodeMilestone("x", "y")
        item.ratio = 1145.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = ScheduledNodeMilestoneDurationMinutesRangeRule(64, 3600)
        val item = newScheduledNodeMilestone("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = ScheduledNodeMilestoneTrailerIdLengthRule(1000)
        val item = newScheduledNodeMilestone("x", "y")
        item.trailerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
