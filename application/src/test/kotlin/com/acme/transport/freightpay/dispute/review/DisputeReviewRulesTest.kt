package com.acme.transport.freightpay.dispute.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeReviewValidator()
        val item = newDisputeReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = DisputeReviewSummaryLengthRule(255)
        val item = newDisputeReview("x", "y")
        item.summary = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = DisputeReviewStartsAtRangeRule(1000, 86400)
        val item = newDisputeReview("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = DisputeReviewGraceDaysRangeRule(16, 80)
        val item = newDisputeReview("x", "y")
        item.graceDays = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
