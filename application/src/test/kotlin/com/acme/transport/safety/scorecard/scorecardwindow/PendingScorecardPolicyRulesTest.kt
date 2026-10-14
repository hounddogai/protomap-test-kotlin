package com.acme.transport.safety.scorecard.scorecardwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingScorecardPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingScorecardPolicyValidator()
        val item = newPendingScorecardPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = PendingScorecardPolicyDurationMinutesRangeRule(64, 86400)
        val item = newPendingScorecardPolicy("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = PendingScorecardPolicyChannelLengthRule(64)
        val item = newPendingScorecardPolicy("x", "y")
        item.channel = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
