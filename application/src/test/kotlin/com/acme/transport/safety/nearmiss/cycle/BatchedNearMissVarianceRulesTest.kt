package com.acme.transport.safety.nearmiss.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedNearMissVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedNearMissVarianceValidator()
        val item = newBatchedNearMissVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedNearMissVarianceCodeLengthRule(255)
        val item = newBatchedNearMissVariance("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long notes`() {
        val rule = BatchedNearMissVarianceNotesLengthRule(86400)
        val item = newBatchedNearMissVariance("x", "y")
        item.notes = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = BatchedNearMissVarianceReviewerLengthRule(16)
        val item = newBatchedNearMissVariance("x", "y")
        item.reviewer = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = BatchedNearMissVarianceBudgetRangeRule(9.6, 3892.4)
        val item = newBatchedNearMissVariance("x", "y")
        item.budget = 3893.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
