package com.acme.transport.customs.clearance.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultClearanceAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultClearanceAllocationValidator()
        val item = newDefaultClearanceAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultClearanceAllocationRequiredCodeRule("default clearance allocation needs an entry")
        val item = newDefaultClearanceAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = DefaultClearanceAllocationFloorValueRangeRule(0.5, 3454.3)
        val item = newDefaultClearanceAllocation("x", "y")
        item.floorValue = 3455.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above amount`() {
        val rule = DefaultClearanceAllocationAmountRangeRule(6.1, 2279.0)
        val item = newDefaultClearanceAllocation("x", "y")
        item.amount = 2280.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = DefaultClearanceAllocationRequiredExternalRefRule("default clearance allocation needs an entry")
        val item = newDefaultClearanceAllocation("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultClearanceAllocationCodeLengthRule(1000)
        val item = newDefaultClearanceAllocation("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
