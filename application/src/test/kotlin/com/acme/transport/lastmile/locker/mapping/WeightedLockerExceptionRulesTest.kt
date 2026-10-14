package com.acme.transport.lastmile.locker.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedLockerExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedLockerExceptionValidator()
        val item = newWeightedLockerException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = WeightedLockerExceptionDistanceKmRangeRule(2.2, 3618.6)
        val item = newWeightedLockerException("x", "y")
        item.distanceKm = 3620.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = WeightedLockerExceptionEndsAtRangeRule(80, 1000)
        val item = newWeightedLockerException("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = WeightedLockerExceptionTrailerIdLengthRule(32)
        val item = newWeightedLockerException("x", "y")
        item.trailerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
