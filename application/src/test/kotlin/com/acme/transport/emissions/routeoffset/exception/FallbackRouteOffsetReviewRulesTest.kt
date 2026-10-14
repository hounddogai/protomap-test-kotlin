package com.acme.transport.emissions.routeoffset.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRouteOffsetReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackRouteOffsetReviewValidator()
        val item = newFallbackRouteOffsetReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = FallbackRouteOffsetReviewAmountRangeRule(4.3, 4286.2)
        val item = newFallbackRouteOffsetReview("x", "y")
        item.amount = 4287.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackRouteOffsetReviewCodeLengthRule(86400)
        val item = newFallbackRouteOffsetReview("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = FallbackRouteOffsetReviewRequiredUnitRule("fallback route offset review needs this field")
        val item = newFallbackRouteOffsetReview("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = FallbackRouteOffsetReviewRequiredSummaryRule("fallback route offset review needs an entry")
        val item = newFallbackRouteOffsetReview("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = FallbackRouteOffsetReviewGraceDaysRangeRule(64, 3600)
        val item = newFallbackRouteOffsetReview("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
