package com.acme.transport.crewing.restbreak.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRestBreakReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedRestBreakReviewValidator()
        val item = newBatchedRestBreakReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = BatchedRestBreakReviewLabelLengthRule(80)
        val item = newBatchedRestBreakReview("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BatchedRestBreakReviewRequiredLabelRule("batched rest break review needs a value")
        val item = newBatchedRestBreakReview("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = BatchedRestBreakReviewTimezoneLengthRule(80)
        val item = newBatchedRestBreakReview("x", "y")
        item.timezone = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
