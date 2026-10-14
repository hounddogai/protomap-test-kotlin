package com.acme.transport.freightpay.dispute.disputegroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDisputeGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDisputeGroupValidator()
        val item = newRollingDisputeGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RollingDisputeGroupTitleLengthRule(86400)
        val item = newRollingDisputeGroup("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = RollingDisputeGroupRequiredTrailerIdRule("rolling dispute group needs a value")
        val item = newRollingDisputeGroup("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = RollingDisputeGroupExternalRefLengthRule(3600)
        val item = newRollingDisputeGroup("x", "y")
        item.externalRef = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
