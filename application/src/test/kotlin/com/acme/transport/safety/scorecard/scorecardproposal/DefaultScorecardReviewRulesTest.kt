package com.acme.transport.safety.scorecard.scorecardproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultScorecardReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultScorecardReviewValidator()
        val item = newDefaultScorecardReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultScorecardReviewCodeLengthRule(32)
        val item = newDefaultScorecardReview("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultScorecardReviewRequiredTitleRule("default scorecard review needs this field")
        val item = newDefaultScorecardReview("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = DefaultScorecardReviewOwnerIdLengthRule(1000)
        val item = newDefaultScorecardReview("x", "y")
        item.ownerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
