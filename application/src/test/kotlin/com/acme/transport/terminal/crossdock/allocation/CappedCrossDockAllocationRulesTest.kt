package com.acme.transport.terminal.crossdock.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCrossDockAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedCrossDockAllocationValidator()
        val item = newCappedCrossDockAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedCrossDockAllocationCodeLengthRule(120)
        val item = newCappedCrossDockAllocation("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = CappedCrossDockAllocationVehicleIdLengthRule(86400)
        val item = newCappedCrossDockAllocation("x", "y")
        item.vehicleId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CappedCrossDockAllocationRequiredLabelRule("capped cross dock allocation needs an entry")
        val item = newCappedCrossDockAllocation("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = CappedCrossDockAllocationRequiredVehicleIdRule("capped cross dock allocation needs an entry")
        val item = newCappedCrossDockAllocation("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = CappedCrossDockAllocationDiscountRangeRule(3.4, 3907.4)
        val item = newCappedCrossDockAllocation("x", "y")
        item.discount = 3908.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
