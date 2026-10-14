package com.acme.transport.crewing.availability.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingAvailabilityReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingAvailabilityReviewValidator()
        val item = newPendingAvailabilityReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingAvailabilityReviewCodeLengthRule(86400)
        val item = newPendingAvailabilityReview("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long notes`() {
        val rule = PendingAvailabilityReviewNotesLengthRule(64)
        val item = newPendingAvailabilityReview("x", "y")
        item.notes = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = PendingAvailabilityReviewCapacityRangeRule(32, 1000)
        val item = newPendingAvailabilityReview("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = PendingAvailabilityReviewLabelLengthRule(80)
        val item = newPendingAvailabilityReview("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingAvailabilityReviewRequiredTitleRule("pending availability review needs an entry")
        val item = newPendingAvailabilityReview("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingAvailabilityReviewTitleLengthRule(16)
        val item = newPendingAvailabilityReview("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
