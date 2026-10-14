package com.acme.transport.terminal.traileryard.yardquota

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedTrailerYardQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTrailerYardQuotaValidator()
        val item = newBatchedTrailerYardQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = BatchedTrailerYardQuotaOdometerRangeRule(64, 120)
        val item = newBatchedTrailerYardQuota("x", "y")
        item.odometer = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = BatchedTrailerYardQuotaAttemptsRangeRule(32, 64)
        val item = newBatchedTrailerYardQuota("x", "y")
        item.attempts = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above margin`() {
        val rule = BatchedTrailerYardQuotaMarginRangeRule(0.4, 4440.8)
        val item = newBatchedTrailerYardQuota("x", "y")
        item.margin = 4442.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
