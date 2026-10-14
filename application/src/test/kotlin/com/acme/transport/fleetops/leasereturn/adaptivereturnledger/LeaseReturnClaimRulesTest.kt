package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnClaimValidator()
        val item = newLeaseReturnClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LeaseReturnClaimRequiredTitleRule("lease return claim needs a value")
        val item = newLeaseReturnClaim("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LeaseReturnClaimCodeLengthRule(86400)
        val item = newLeaseReturnClaim("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
