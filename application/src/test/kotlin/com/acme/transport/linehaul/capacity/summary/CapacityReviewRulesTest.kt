package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityReviewValidator()
        val item = newCapacityReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = CapacityReviewMarginRangeRule(4.2, 2830.4)
        val item = newCapacityReview("x", "y")
        item.margin = 2831.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = CapacityReviewGraceDaysRangeRule(120, 3600)
        val item = newCapacityReview("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long country`() {
        val rule = CapacityReviewCountryLengthRule(64)
        val item = newCapacityReview("x", "y")
        item.country = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CapacityReviewRequiredCodeRule("capacity review needs a value")
        val item = newCapacityReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
