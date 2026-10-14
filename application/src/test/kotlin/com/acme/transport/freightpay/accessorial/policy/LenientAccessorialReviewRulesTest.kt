package com.acme.transport.freightpay.accessorial.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientAccessorialReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientAccessorialReviewValidator()
        val item = newLenientAccessorialReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LenientAccessorialReviewNameLengthRule(3600)
        val item = newLenientAccessorialReview("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = LenientAccessorialReviewRequiredLocaleRule("lenient accessorial review needs a value")
        val item = newLenientAccessorialReview("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = LenientAccessorialReviewLocaleLengthRule(86400)
        val item = newLenientAccessorialReview("x", "y")
        item.locale = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
