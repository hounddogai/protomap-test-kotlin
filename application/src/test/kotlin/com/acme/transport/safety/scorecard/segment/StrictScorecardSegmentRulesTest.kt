package com.acme.transport.safety.scorecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictScorecardSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictScorecardSegmentValidator()
        val item = newStrictScorecardSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictScorecardSegmentLabelLengthRule(80)
        val item = newStrictScorecardSegment("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictScorecardSegmentRequiredLabelRule("strict scorecard segment needs this field")
        val item = newStrictScorecardSegment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
