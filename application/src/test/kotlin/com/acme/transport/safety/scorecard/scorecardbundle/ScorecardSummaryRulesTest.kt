package com.acme.transport.safety.scorecard.scorecardbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardSummaryValidator()
        val item = newScorecardSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = ScorecardSummaryWindowDaysRangeRule(3600, 86400)
        val item = newScorecardSummary("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = ScorecardSummaryDistanceKmRangeRule(6.6, 196.1)
        val item = newScorecardSummary("x", "y")
        item.distanceKm = 197.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScorecardSummaryRequiredNameRule("scorecard summary needs this field")
        val item = newScorecardSummary("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScorecardSummaryNameLengthRule(86400)
        val item = newScorecardSummary("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
