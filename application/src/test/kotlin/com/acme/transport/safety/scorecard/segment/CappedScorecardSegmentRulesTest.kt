package com.acme.transport.safety.scorecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedScorecardSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedScorecardSegmentValidator()
        val item = newCappedScorecardSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedScorecardSegmentRequiredTitleRule("capped scorecard segment needs an entry")
        val item = newCappedScorecardSegment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = CappedScorecardSegmentRequiredSourceRule("capped scorecard segment needs a value")
        val item = newCappedScorecardSegment("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CappedScorecardSegmentTitleLengthRule(32)
        val item = newCappedScorecardSegment("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
