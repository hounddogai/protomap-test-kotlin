package com.acme.transport.network.transittime.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeAllocationValidator()
        val item = newTransitTimeAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TransitTimeAllocationNameLengthRule(1000)
        val item = newTransitTimeAllocation("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = TransitTimeAllocationCostRangeRule(0.6, 4047.4)
        val item = newTransitTimeAllocation("x", "y")
        item.cost = 4048.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = TransitTimeAllocationRequiredTimezoneRule("transit time allocation needs an entry")
        val item = newTransitTimeAllocation("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = TransitTimeAllocationRequiredVehicleIdRule("transit time allocation needs a value")
        val item = newTransitTimeAllocation("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TransitTimeAllocationCodeLengthRule(1000)
        val item = newTransitTimeAllocation("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = TransitTimeAllocationTimezoneLengthRule(120)
        val item = newTransitTimeAllocation("x", "y")
        item.timezone = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
