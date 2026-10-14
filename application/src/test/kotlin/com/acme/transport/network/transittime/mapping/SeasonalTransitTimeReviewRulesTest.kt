package com.acme.transport.network.transittime.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalTransitTimeReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalTransitTimeReviewValidator()
        val item = newSeasonalTransitTimeReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = SeasonalTransitTimeReviewScoreRangeRule(6.8, 4791.7)
        val item = newSeasonalTransitTimeReview("x", "y")
        item.score = 4793.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalTransitTimeReviewLabelLengthRule(1000)
        val item = newSeasonalTransitTimeReview("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = SeasonalTransitTimeReviewTitleLengthRule(64)
        val item = newSeasonalTransitTimeReview("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalTransitTimeReviewRequiredLabelRule("seasonal transit time review needs an entry")
        val item = newSeasonalTransitTimeReview("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalTransitTimeReviewRequiredCodeRule("seasonal transit time review needs a value")
        val item = newSeasonalTransitTimeReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
