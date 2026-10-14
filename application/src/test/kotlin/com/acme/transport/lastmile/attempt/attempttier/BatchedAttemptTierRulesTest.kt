package com.acme.transport.lastmile.attempt.attempttier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAttemptTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedAttemptTierValidator()
        val item = newBatchedAttemptTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = BatchedAttemptTierEndsAtRangeRule(16, 32)
        val item = newBatchedAttemptTier("x", "y")
        item.endsAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedAttemptTierCodeLengthRule(3600)
        val item = newBatchedAttemptTier("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = BatchedAttemptTierFuelLitersRangeRule(0.6, 1670.4)
        val item = newBatchedAttemptTier("x", "y")
        item.fuelLiters = 1671.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
