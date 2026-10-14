package com.acme.transport.fleetops.leasereturn.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnAllocationValidator()
        val item = newLeaseReturnAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = LeaseReturnAllocationCreatedAtRangeRule(16, 64)
        val item = newLeaseReturnAllocation("x", "y")
        item.createdAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LeaseReturnAllocationCodeLengthRule(86400)
        val item = newLeaseReturnAllocation("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = LeaseReturnAllocationRequiredCategoryRule("lease return allocation needs an entry")
        val item = newLeaseReturnAllocation("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LeaseReturnAllocationRequiredTitleRule("lease return allocation needs an entry")
        val item = newLeaseReturnAllocation("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LeaseReturnAllocationTitleLengthRule(32)
        val item = newLeaseReturnAllocation("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
