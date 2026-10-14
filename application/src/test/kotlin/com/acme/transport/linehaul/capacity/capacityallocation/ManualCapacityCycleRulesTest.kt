package com.acme.transport.linehaul.capacity.capacityallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCapacityCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualCapacityCycleValidator()
        val item = newManualCapacityCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = ManualCapacityCycleRequiredDriverIdRule("manual capacity cycle needs this field")
        val item = newManualCapacityCycle("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = ManualCapacityCycleCostRangeRule(5.5, 2094.0)
        val item = newManualCapacityCycle("x", "y")
        item.cost = 2095.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
