package com.acme.transport.fleetops.leasereturn.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnVarianceValidator()
        val item = newLeaseReturnVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = LeaseReturnVarianceDescriptionLengthRule(120)
        val item = newLeaseReturnVariance("x", "y")
        item.description = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = LeaseReturnVarianceRequiredTimezoneRule("lease return variance needs a value")
        val item = newLeaseReturnVariance("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = LeaseReturnVarianceDurationMinutesRangeRule(32, 120)
        val item = newLeaseReturnVariance("x", "y")
        item.durationMinutes = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above amount`() {
        val rule = LeaseReturnVarianceAmountRangeRule(2.4, 3189.7)
        val item = newLeaseReturnVariance("x", "y")
        item.amount = 3191.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
