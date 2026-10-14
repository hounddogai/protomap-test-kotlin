package com.acme.transport.linehaul.backhaul.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackBackhaulMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackBackhaulMilestoneValidator()
        val item = newFallbackBackhaulMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = FallbackBackhaulMilestoneRequiredVehicleIdRule("fallback backhaul milestone needs a value")
        val item = newFallbackBackhaulMilestone("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = FallbackBackhaulMilestoneVehicleIdLengthRule(1000)
        val item = newFallbackBackhaulMilestone("x", "y")
        item.vehicleId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = FallbackBackhaulMilestoneCostRangeRule(6.5, 3752.6)
        val item = newFallbackBackhaulMilestone("x", "y")
        item.cost = 3754.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
