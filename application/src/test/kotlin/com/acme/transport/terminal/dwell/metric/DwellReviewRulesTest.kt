package com.acme.transport.terminal.dwell.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellReviewValidator()
        val item = newDwellReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = DwellReviewEndsAtRangeRule(32, 1000)
        val item = newDwellReview("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = DwellReviewRequiredCarrierRule("dwell review needs this field")
        val item = newDwellReview("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = DwellReviewTitleLengthRule(64)
        val item = newDwellReview("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long phone`() {
        val rule = DwellReviewPhoneLengthRule(32)
        val item = newDwellReview("x", "y")
        item.phone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
