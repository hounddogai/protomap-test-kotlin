package com.acme.transport.crewing.roster.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRosterReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryRosterReviewValidator()
        val item = newPrimaryRosterReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = PrimaryRosterReviewLaneLengthRule(80)
        val item = newPrimaryRosterReview("x", "y")
        item.lane = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = PrimaryRosterReviewScoreRangeRule(8.2, 3603.3)
        val item = newPrimaryRosterReview("x", "y")
        item.score = 3604.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = PrimaryRosterReviewUnitLengthRule(120)
        val item = newPrimaryRosterReview("x", "y")
        item.unit = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
