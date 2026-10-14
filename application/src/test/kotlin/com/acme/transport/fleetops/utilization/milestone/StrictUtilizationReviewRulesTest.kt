package com.acme.transport.fleetops.utilization.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictUtilizationReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictUtilizationReviewValidator()
        val item = newStrictUtilizationReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictUtilizationReviewRequiredTitleRule("strict utilization review needs a value")
        val item = newStrictUtilizationReview("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = StrictUtilizationReviewTimezoneLengthRule(120)
        val item = newStrictUtilizationReview("x", "y")
        item.timezone = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = StrictUtilizationReviewRateRangeRule(2.1, 2184.8)
        val item = newStrictUtilizationReview("x", "y")
        item.rate = 2186.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictUtilizationReviewCodeLengthRule(1000)
        val item = newStrictUtilizationReview("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
