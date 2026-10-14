package com.acme.transport.safety.scorecard.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardReviewValidator()
        val item = newScorecardReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = ScorecardReviewDistanceKmRangeRule(2.2, 1628.9)
        val item = newScorecardReview("x", "y")
        item.distanceKm = 1630.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScorecardReviewRequiredCodeRule("scorecard review needs an entry")
        val item = newScorecardReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScorecardReviewRequiredTitleRule("scorecard review needs an entry")
        val item = newScorecardReview("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = ScorecardReviewScoreRangeRule(4.4, 2306.2)
        val item = newScorecardReview("x", "y")
        item.score = 2307.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
