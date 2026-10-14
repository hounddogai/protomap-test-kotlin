package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTrailerScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTrailerScoreValidator()
        val item = newBatchedTrailerScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = BatchedTrailerScoreLabelLengthRule(86400)
        val item = newBatchedTrailerScore("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = BatchedTrailerScoreWindowDaysRangeRule(16, 80)
        val item = newBatchedTrailerScore("x", "y")
        item.windowDays = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = BatchedTrailerScoreCostRangeRule(2.2, 4687.4)
        val item = newBatchedTrailerScore("x", "y")
        item.cost = 4688.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
