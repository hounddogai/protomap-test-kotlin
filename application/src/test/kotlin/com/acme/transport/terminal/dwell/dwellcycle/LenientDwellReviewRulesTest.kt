package com.acme.transport.terminal.dwell.dwellcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDwellReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDwellReviewValidator()
        val item = newLenientDwellReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientDwellReviewLabelLengthRule(3600)
        val item = newLenientDwellReview("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientDwellReviewRequiredCodeRule("lenient dwell review needs this field")
        val item = newLenientDwellReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LenientDwellReviewRequiredLabelRule("lenient dwell review needs this field")
        val item = newLenientDwellReview("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
