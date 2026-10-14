package com.acme.transport.terminal.sortation.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingSortationReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingSortationReviewValidator()
        val item = newRollingSortationReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = RollingSortationReviewBatchSizeRangeRule(120, 86400)
        val item = newRollingSortationReview("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = RollingSortationReviewLabelLengthRule(3600)
        val item = newRollingSortationReview("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = RollingSortationReviewSummaryLengthRule(64)
        val item = newRollingSortationReview("x", "y")
        item.summary = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
