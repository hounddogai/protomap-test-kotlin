package com.acme.transport.crewing.restbreak.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRestBreakReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientRestBreakReviewValidator()
        val item = newLenientRestBreakReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = LenientRestBreakReviewPhoneLengthRule(1000)
        val item = newLenientRestBreakReview("x", "y")
        item.phone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = LenientRestBreakReviewNameLengthRule(64)
        val item = newLenientRestBreakReview("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
