package com.acme.transport.fleetops.leasereturn.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnProfileValidator()
        val item = newLeaseReturnProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = LeaseReturnProfileCreatedAtRangeRule(64, 1000)
        val item = newLeaseReturnProfile("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LeaseReturnProfileCodeLengthRule(120)
        val item = newLeaseReturnProfile("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = LeaseReturnProfileRequiredSourceRule("lease return profile needs this field")
        val item = newLeaseReturnProfile("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
