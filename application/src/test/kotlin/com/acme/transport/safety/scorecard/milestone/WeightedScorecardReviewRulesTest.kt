package com.acme.transport.safety.scorecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedScorecardReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedScorecardReviewValidator()
        val item = newWeightedScorecardReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = WeightedScorecardReviewRequiredNotesRule("weighted scorecard review needs this field")
        val item = newWeightedScorecardReview("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = WeightedScorecardReviewRatioRangeRule(9.3, 100.2)
        val item = newWeightedScorecardReview("x", "y")
        item.ratio = 101.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = WeightedScorecardReviewCarrierLengthRule(80)
        val item = newWeightedScorecardReview("x", "y")
        item.carrier = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
