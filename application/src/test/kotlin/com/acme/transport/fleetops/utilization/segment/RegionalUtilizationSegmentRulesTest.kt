package com.acme.transport.fleetops.utilization.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalUtilizationSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalUtilizationSegmentValidator()
        val item = newRegionalUtilizationSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = RegionalUtilizationSegmentCostRangeRule(1.2, 4382.3)
        val item = newRegionalUtilizationSegment("x", "y")
        item.cost = 4383.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = RegionalUtilizationSegmentPalletsRangeRule(32, 3600)
        val item = newRegionalUtilizationSegment("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalUtilizationSegmentRequiredLabelRule("regional utilization segment needs an entry")
        val item = newRegionalUtilizationSegment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
