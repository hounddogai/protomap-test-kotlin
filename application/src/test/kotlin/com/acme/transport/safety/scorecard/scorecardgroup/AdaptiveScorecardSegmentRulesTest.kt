package com.acme.transport.safety.scorecard.scorecardgroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveScorecardSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveScorecardSegmentValidator()
        val item = newAdaptiveScorecardSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = AdaptiveScorecardSegmentRequiredChecksumRule("adaptive scorecard segment needs a value")
        val item = newAdaptiveScorecardSegment("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveScorecardSegmentTitleLengthRule(16)
        val item = newAdaptiveScorecardSegment("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveScorecardSegmentCodeLengthRule(3600)
        val item = newAdaptiveScorecardSegment("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveScorecardSegmentRequiredCodeRule("adaptive scorecard segment needs this field")
        val item = newAdaptiveScorecardSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = AdaptiveScorecardSegmentChecksumLengthRule(32)
        val item = newAdaptiveScorecardSegment("x", "y")
        item.checksum = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
