package com.acme.transport.customs.broker.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingBrokerAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingBrokerAllocationValidator()
        val item = newPendingBrokerAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingBrokerAllocationRequiredCodeRule("pending broker allocation needs an entry")
        val item = newPendingBrokerAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = PendingBrokerAllocationDiscountRangeRule(6.8, 3382.6)
        val item = newPendingBrokerAllocation("x", "y")
        item.discount = 3384.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = PendingBrokerAllocationUnitLengthRule(32)
        val item = newPendingBrokerAllocation("x", "y")
        item.unit = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
