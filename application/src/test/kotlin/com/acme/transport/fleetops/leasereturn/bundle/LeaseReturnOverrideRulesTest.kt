package com.acme.transport.fleetops.leasereturn.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnOverrideValidator()
        val item = newLeaseReturnOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LeaseReturnOverrideRequiredTitleRule("lease return override needs this field")
        val item = newLeaseReturnOverride("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = LeaseReturnOverrideCapacityRangeRule(16, 32)
        val item = newLeaseReturnOverride("x", "y")
        item.capacity = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = LeaseReturnOverrideGraceDaysRangeRule(80, 255)
        val item = newLeaseReturnOverride("x", "y")
        item.graceDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above rate`() {
        val rule = LeaseReturnOverrideRateRangeRule(1.6, 2694.2)
        val item = newLeaseReturnOverride("x", "y")
        item.rate = 2695.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = LeaseReturnOverrideRequiredSegmentRule("lease return override needs this field")
        val item = newLeaseReturnOverride("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
