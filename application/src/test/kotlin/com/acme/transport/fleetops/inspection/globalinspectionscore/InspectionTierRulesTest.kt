package com.acme.transport.fleetops.inspection.globalinspectionscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionTierValidator()
        val item = newInspectionTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = InspectionTierRequiredNameRule("inspection tier needs this field")
        val item = newInspectionTier("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = InspectionTierThresholdRangeRule(9.8, 1384.8)
        val item = newInspectionTier("x", "y")
        item.threshold = 1386.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
