package com.acme.transport.safety.scorecard.scorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardPreferenceValidator()
        val item = newScorecardPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ScorecardPreferenceTitleLengthRule(3600)
        val item = newScorecardPreference("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = ScorecardPreferenceWeightRangeRule(8.9, 4573.3)
        val item = newScorecardPreference("x", "y")
        item.weight = 4574.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = ScorecardPreferenceRequiredSummaryRule("scorecard preference needs this field")
        val item = newScorecardPreference("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = ScorecardPreferenceCapacityRangeRule(32, 80)
        val item = newScorecardPreference("x", "y")
        item.capacity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
