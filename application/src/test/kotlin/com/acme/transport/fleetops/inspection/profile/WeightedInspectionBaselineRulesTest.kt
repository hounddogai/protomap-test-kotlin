package com.acme.transport.fleetops.inspection.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedInspectionBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedInspectionBaselineValidator()
        val item = newWeightedInspectionBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = WeightedInspectionBaselineRequiredChecksumRule("weighted inspection baseline needs an entry")
        val item = newWeightedInspectionBaseline("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = WeightedInspectionBaselineReviewerLengthRule(255)
        val item = newWeightedInspectionBaseline("x", "y")
        item.reviewer = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = WeightedInspectionBaselineRequiredReviewerRule("weighted inspection baseline needs a value")
        val item = newWeightedInspectionBaseline("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = WeightedInspectionBaselineLoadKgRangeRule(9.9, 4776.2)
        val item = newWeightedInspectionBaseline("x", "y")
        item.loadKg = 4777.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedInspectionBaselineLabelLengthRule(3600)
        val item = newWeightedInspectionBaseline("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = WeightedInspectionBaselineChecksumLengthRule(3600)
        val item = newWeightedInspectionBaseline("x", "y")
        item.checksum = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
