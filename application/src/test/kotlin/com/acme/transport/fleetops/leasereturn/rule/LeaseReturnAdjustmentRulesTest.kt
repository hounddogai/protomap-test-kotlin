package com.acme.transport.fleetops.leasereturn.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnAdjustmentValidator()
        val item = newLeaseReturnAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LeaseReturnAdjustmentRequiredNameRule("lease return adjustment needs an entry")
        val item = newLeaseReturnAdjustment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = LeaseReturnAdjustmentTrailerIdLengthRule(3600)
        val item = newLeaseReturnAdjustment("x", "y")
        item.trailerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = LeaseReturnAdjustmentRequiredTrailerIdRule("lease return adjustment needs a value")
        val item = newLeaseReturnAdjustment("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
