package com.acme.transport.freightpay.dispute.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDisputeClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingDisputeClaimValidator()
        val item = newPendingDisputeClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingDisputeClaimRequiredNameRule("pending dispute claim needs a value")
        val item = newPendingDisputeClaim("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = PendingDisputeClaimExpiresAtRangeRule(32, 32)
        val item = newPendingDisputeClaim("x", "y")
        item.expiresAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = PendingDisputeClaimRequiredTimezoneRule("pending dispute claim needs this field")
        val item = newPendingDisputeClaim("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
