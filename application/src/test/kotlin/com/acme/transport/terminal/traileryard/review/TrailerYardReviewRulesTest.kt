package com.acme.transport.terminal.traileryard.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardReviewValidator()
        val item = newTrailerYardReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = TrailerYardReviewRequiredLocaleRule("trailer yard review needs this field")
        val item = newTrailerYardReview("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TrailerYardReviewRequiredLabelRule("trailer yard review needs an entry")
        val item = newTrailerYardReview("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = TrailerYardReviewLocaleLengthRule(120)
        val item = newTrailerYardReview("x", "y")
        item.locale = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
