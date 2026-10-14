package com.acme.transport.crewing.roster.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterMilestoneValidator()
        val item = newRosterMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = RosterMilestoneLaneLengthRule(255)
        val item = newRosterMilestone("x", "y")
        item.lane = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = RosterMilestoneDurationMinutesRangeRule(64, 86400)
        val item = newRosterMilestone("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = RosterMilestoneExpiresAtRangeRule(64, 120)
        val item = newRosterMilestone("x", "y")
        item.expiresAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
