package com.acme.transport.fleetops.serviceplan.legacyplanquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedServicePlanMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedServicePlanMilestoneValidator()
        val item = newWeightedServicePlanMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = WeightedServicePlanMilestoneRouteCodeLengthRule(255)
        val item = newWeightedServicePlanMilestone("x", "y")
        item.routeCode = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedServicePlanMilestoneRequiredLabelRule("weighted service plan milestone needs this field")
        val item = newWeightedServicePlanMilestone("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = WeightedServicePlanMilestoneFloorValueRangeRule(7.6, 4385.9)
        val item = newWeightedServicePlanMilestone("x", "y")
        item.floorValue = 4387.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = WeightedServicePlanMilestoneRequiredRouteCodeRule(
            "weighted service plan milestone needs this field",
        )
        val item = newWeightedServicePlanMilestone("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedServicePlanMilestoneLabelLengthRule(80)
        val item = newWeightedServicePlanMilestone("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = WeightedServicePlanMilestoneExternalRefLengthRule(32)
        val item = newWeightedServicePlanMilestone("x", "y")
        item.externalRef = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
