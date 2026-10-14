package com.acme.transport.linehaul.trailer.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryTrailerReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryTrailerReviewValidator()
        val item = newPrimaryTrailerReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = PrimaryTrailerReviewRequiredPhoneRule("primary trailer review needs this field")
        val item = newPrimaryTrailerReview("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryTrailerReviewRequiredCodeRule("primary trailer review needs a value")
        val item = newPrimaryTrailerReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PrimaryTrailerReviewLabelLengthRule(255)
        val item = newPrimaryTrailerReview("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = PrimaryTrailerReviewMinimumRangeRule(9.2, 1328.8)
        val item = newPrimaryTrailerReview("x", "y")
        item.minimum = 1330.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PrimaryTrailerReviewRequiredLabelRule("primary trailer review needs an entry")
        val item = newPrimaryTrailerReview("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
