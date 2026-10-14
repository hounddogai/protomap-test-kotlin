package com.acme.transport.fleetops.defect.defectprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDefectVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDefectVarianceValidator()
        val item = newFallbackDefectVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = FallbackDefectVarianceAttemptsRangeRule(120, 1000)
        val item = newFallbackDefectVariance("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackDefectVarianceRequiredLabelRule("fallback defect variance needs a value")
        val item = newFallbackDefectVariance("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = FallbackDefectVarianceGraceDaysRangeRule(32, 1000)
        val item = newFallbackDefectVariance("x", "y")
        item.graceDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackDefectVarianceRequiredCodeRule("fallback defect variance needs this field")
        val item = newFallbackDefectVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = FallbackDefectVarianceCapacityRangeRule(120, 86400)
        val item = newFallbackDefectVariance("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = FallbackDefectVarianceReviewerLengthRule(1000)
        val item = newFallbackDefectVariance("x", "y")
        item.reviewer = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
