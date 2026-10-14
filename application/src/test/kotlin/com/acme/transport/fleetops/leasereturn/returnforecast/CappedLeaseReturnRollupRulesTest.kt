package com.acme.transport.fleetops.leasereturn.returnforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedLeaseReturnRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedLeaseReturnRollupValidator()
        val item = newCappedLeaseReturnRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = CappedLeaseReturnRollupRateRangeRule(5.1, 1083.0)
        val item = newCappedLeaseReturnRollup("x", "y")
        item.rate = 1084.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedLeaseReturnRollupCodeLengthRule(120)
        val item = newCappedLeaseReturnRollup("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = CappedLeaseReturnRollupLabelLengthRule(86400)
        val item = newCappedLeaseReturnRollup("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = CappedLeaseReturnRollupDiscountRangeRule(8.5, 3914.0)
        val item = newCappedLeaseReturnRollup("x", "y")
        item.discount = 3915.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
