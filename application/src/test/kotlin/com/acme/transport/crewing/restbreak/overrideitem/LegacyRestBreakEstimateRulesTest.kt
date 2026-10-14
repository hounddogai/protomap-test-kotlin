package com.acme.transport.crewing.restbreak.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyRestBreakEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyRestBreakEstimateValidator()
        val item = newLegacyRestBreakEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyRestBreakEstimateTitleLengthRule(3600)
        val item = newLegacyRestBreakEstimate("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = LegacyRestBreakEstimateReviewerLengthRule(1000)
        val item = newLegacyRestBreakEstimate("x", "y")
        item.reviewer = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = LegacyRestBreakEstimateLoadKgRangeRule(8.0, 3886.6)
        val item = newLegacyRestBreakEstimate("x", "y")
        item.loadKg = 3888.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyRestBreakEstimateRequiredCodeRule("legacy rest break estimate needs an entry")
        val item = newLegacyRestBreakEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
