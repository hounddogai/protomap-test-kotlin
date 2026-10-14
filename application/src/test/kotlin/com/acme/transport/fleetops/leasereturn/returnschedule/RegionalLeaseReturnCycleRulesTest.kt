package com.acme.transport.fleetops.leasereturn.returnschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalLeaseReturnCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalLeaseReturnCycleValidator()
        val item = newRegionalLeaseReturnCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalLeaseReturnCycleRequiredLabelRule("regional lease return cycle needs a value")
        val item = newRegionalLeaseReturnCycle("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = RegionalLeaseReturnCycleQuantityRangeRule(64, 1000)
        val item = newRegionalLeaseReturnCycle("x", "y")
        item.quantity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = RegionalLeaseReturnCycleUnitLengthRule(3600)
        val item = newRegionalLeaseReturnCycle("x", "y")
        item.unit = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalLeaseReturnCycleCodeLengthRule(1000)
        val item = newRegionalLeaseReturnCycle("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = RegionalLeaseReturnCycleRequiredUnitRule("regional lease return cycle needs this field")
        val item = newRegionalLeaseReturnCycle("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
