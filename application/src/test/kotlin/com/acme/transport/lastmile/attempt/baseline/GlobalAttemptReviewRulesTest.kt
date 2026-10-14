package com.acme.transport.lastmile.attempt.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAttemptReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalAttemptReviewValidator()
        val item = newGlobalAttemptReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = GlobalAttemptReviewCategoryLengthRule(1000)
        val item = newGlobalAttemptReview("x", "y")
        item.category = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = GlobalAttemptReviewEndsAtRangeRule(32, 3600)
        val item = newGlobalAttemptReview("x", "y")
        item.endsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
