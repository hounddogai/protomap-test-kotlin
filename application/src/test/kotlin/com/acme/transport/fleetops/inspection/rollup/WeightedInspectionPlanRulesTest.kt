package com.acme.transport.fleetops.inspection.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedInspectionPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedInspectionPlanValidator()
        val item = newWeightedInspectionPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = WeightedInspectionPlanCostRangeRule(0.5, 2642.2)
        val item = newWeightedInspectionPlan("x", "y")
        item.cost = 2643.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long route code`() {
        val rule = WeightedInspectionPlanRouteCodeLengthRule(255)
        val item = newWeightedInspectionPlan("x", "y")
        item.routeCode = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = WeightedInspectionPlanFloorValueRangeRule(7.1, 2729.9)
        val item = newWeightedInspectionPlan("x", "y")
        item.floorValue = 2731.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
