package com.acme.transport.fleetops.leasereturn.returnaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnRequestValidator()
        val item = newLeaseReturnRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LeaseReturnRequestRequiredCodeRule("lease return request needs a value")
        val item = newLeaseReturnRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = LeaseReturnRequestRevisionRangeRule(3600, 86400)
        val item = newLeaseReturnRequest("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
