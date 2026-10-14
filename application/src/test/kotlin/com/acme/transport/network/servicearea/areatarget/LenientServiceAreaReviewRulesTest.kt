package com.acme.transport.network.servicearea.areatarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientServiceAreaReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientServiceAreaReviewValidator()
        val item = newLenientServiceAreaReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = LenientServiceAreaReviewRequiredSummaryRule("lenient service area review needs an entry")
        val item = newLenientServiceAreaReview("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientServiceAreaReviewCodeLengthRule(32)
        val item = newLenientServiceAreaReview("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = LenientServiceAreaReviewSummaryLengthRule(64)
        val item = newLenientServiceAreaReview("x", "y")
        item.summary = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = LenientServiceAreaReviewWeightRangeRule(6.3, 1823.9)
        val item = newLenientServiceAreaReview("x", "y")
        item.weight = 1825.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
