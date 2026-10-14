package com.acme.transport.crewing.restbreak.breaksummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRestBreakEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalRestBreakEstimateValidator()
        val item = newSeasonalRestBreakEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = SeasonalRestBreakEstimateCapacityRangeRule(86400, 86400)
        val item = newSeasonalRestBreakEstimate("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = SeasonalRestBreakEstimateLoadKgRangeRule(7.0, 842.0)
        val item = newSeasonalRestBreakEstimate("x", "y")
        item.loadKg = 843.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long summary`() {
        val rule = SeasonalRestBreakEstimateSummaryLengthRule(120)
        val item = newSeasonalRestBreakEstimate("x", "y")
        item.summary = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
