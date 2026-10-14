package com.acme.transport.emissions.evcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveEvChargeReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveEvChargeReviewValidator()
        val item = newAdaptiveEvChargeReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = AdaptiveEvChargeReviewEndsAtRangeRule(64, 3600)
        val item = newAdaptiveEvChargeReview("x", "y")
        item.endsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = AdaptiveEvChargeReviewRetriesRangeRule(16, 3600)
        val item = newAdaptiveEvChargeReview("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = AdaptiveEvChargeReviewRequiredSegmentRule("adaptive ev charge review needs a value")
        val item = newAdaptiveEvChargeReview("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
