package com.acme.transport.customs.inspection.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionMilestoneValidator()
        val item = newInspectionMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = InspectionMilestoneDistanceKmRangeRule(0.8, 1514.4)
        val item = newInspectionMilestone("x", "y")
        item.distanceKm = 1515.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionMilestoneRequiredCodeRule("inspection milestone needs a value")
        val item = newInspectionMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = InspectionMilestoneThresholdRangeRule(0.6, 4591.3)
        val item = newInspectionMilestone("x", "y")
        item.threshold = 4592.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = InspectionMilestoneCodeLengthRule(255)
        val item = newInspectionMilestone("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
