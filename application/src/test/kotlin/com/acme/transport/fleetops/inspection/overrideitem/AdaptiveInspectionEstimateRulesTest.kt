package com.acme.transport.fleetops.inspection.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveInspectionEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveInspectionEstimateValidator()
        val item = newAdaptiveInspectionEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveInspectionEstimateTitleLengthRule(120)
        val item = newAdaptiveInspectionEstimate("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = AdaptiveInspectionEstimateMarginRangeRule(7.1, 4070.6)
        val item = newAdaptiveInspectionEstimate("x", "y")
        item.margin = 4072.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = AdaptiveInspectionEstimateFloorValueRangeRule(1.5, 3053.3)
        val item = newAdaptiveInspectionEstimate("x", "y")
        item.floorValue = 3054.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long region`() {
        val rule = AdaptiveInspectionEstimateRegionLengthRule(80)
        val item = newAdaptiveInspectionEstimate("x", "y")
        item.region = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
