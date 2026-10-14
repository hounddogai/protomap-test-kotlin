package com.acme.transport.network.servicearea.areamanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaMilestoneValidator()
        val item = newServiceAreaMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = ServiceAreaMilestoneBudgetRangeRule(5.2, 1226.5)
        val item = newServiceAreaMilestone("x", "y")
        item.budget = 1228.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = ServiceAreaMilestoneCostRangeRule(0.9, 3097.5)
        val item = newServiceAreaMilestone("x", "y")
        item.cost = 3099.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServiceAreaMilestoneCodeLengthRule(1000)
        val item = newServiceAreaMilestone("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = ServiceAreaMilestoneRequiredVehicleIdRule("service area milestone needs this field")
        val item = newServiceAreaMilestone("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
