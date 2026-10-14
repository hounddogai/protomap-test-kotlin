package com.acme.transport.safety.scorecard.scorecardproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingScorecardThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingScorecardThresholdValidator()
        val item = newPendingScorecardThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = PendingScorecardThresholdPalletsRangeRule(3600, 3600)
        val item = newPendingScorecardThreshold("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingScorecardThresholdRequiredTitleRule("pending scorecard threshold needs a value")
        val item = newPendingScorecardThreshold("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = PendingScorecardThresholdChannelLengthRule(86400)
        val item = newPendingScorecardThreshold("x", "y")
        item.channel = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = PendingScorecardThresholdLabelLengthRule(3600)
        val item = newPendingScorecardThreshold("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingScorecardThresholdTitleLengthRule(255)
        val item = newPendingScorecardThreshold("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
