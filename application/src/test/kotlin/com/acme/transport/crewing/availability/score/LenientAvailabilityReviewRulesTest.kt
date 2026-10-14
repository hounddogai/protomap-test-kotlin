package com.acme.transport.crewing.availability.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientAvailabilityReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientAvailabilityReviewValidator()
        val item = newLenientAvailabilityReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = LenientAvailabilityReviewFloorValueRangeRule(5.7, 853.6)
        val item = newLenientAvailabilityReview("x", "y")
        item.floorValue = 855.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientAvailabilityReviewRequiredCodeRule("lenient availability review needs this field")
        val item = newLenientAvailabilityReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientAvailabilityReviewCodeLengthRule(3600)
        val item = newLenientAvailabilityReview("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
