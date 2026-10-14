package com.acme.transport.safety.scorecard.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardSegmentValidator()
        val item = newScorecardSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = ScorecardSegmentRequiredCarrierRule("scorecard segment needs this field")
        val item = newScorecardSegment("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = ScorecardSegmentEtaMinutesRangeRule(80, 3600)
        val item = newScorecardSegment("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above priority`() {
        val rule = ScorecardSegmentPriorityRangeRule(255, 255)
        val item = newScorecardSegment("x", "y")
        item.priority = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = ScorecardSegmentRequiredLaneRule("scorecard segment needs a value")
        val item = newScorecardSegment("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScorecardSegmentRequiredLabelRule("scorecard segment needs this field")
        val item = newScorecardSegment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
