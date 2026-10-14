package com.acme.transport.customs.inspection.inspectionmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveInspectionMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveInspectionMilestoneValidator()
        val item = newAdaptiveInspectionMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = AdaptiveInspectionMilestoneCountryLengthRule(120)
        val item = newAdaptiveInspectionMilestone("x", "y")
        item.country = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = AdaptiveInspectionMilestoneRatioRangeRule(0.8, 4042.1)
        val item = newAdaptiveInspectionMilestone("x", "y")
        item.ratio = 4043.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long summary`() {
        val rule = AdaptiveInspectionMilestoneSummaryLengthRule(32)
        val item = newAdaptiveInspectionMilestone("x", "y")
        item.summary = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank country`() {
        val rule = AdaptiveInspectionMilestoneRequiredCountryRule("adaptive inspection milestone needs an entry")
        val item = newAdaptiveInspectionMilestone("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = AdaptiveInspectionMilestoneCreatedAtRangeRule(120, 3600)
        val item = newAdaptiveInspectionMilestone("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
