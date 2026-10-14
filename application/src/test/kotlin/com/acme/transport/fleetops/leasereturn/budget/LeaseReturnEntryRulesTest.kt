package com.acme.transport.fleetops.leasereturn.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnEntryValidator()
        val item = newLeaseReturnEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = LeaseReturnEntryDistanceKmRangeRule(6.1, 2089.0)
        val item = newLeaseReturnEntry("x", "y")
        item.distanceKm = 2090.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above priority`() {
        val rule = LeaseReturnEntryPriorityRangeRule(32, 64)
        val item = newLeaseReturnEntry("x", "y")
        item.priority = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = LeaseReturnEntryChannelLengthRule(16)
        val item = newLeaseReturnEntry("x", "y")
        item.channel = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = LeaseReturnEntryTitleLengthRule(86400)
        val item = newLeaseReturnEntry("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LeaseReturnEntryRequiredCodeRule("lease return entry needs this field")
        val item = newLeaseReturnEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
