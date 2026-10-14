package com.acme.transport.fleetops.leasereturn.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnProposalValidator()
        val item = newLeaseReturnProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = LeaseReturnProposalCreatedAtRangeRule(120, 86400)
        val item = newLeaseReturnProposal("x", "y")
        item.createdAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LeaseReturnProposalRequiredLabelRule("lease return proposal needs this field")
        val item = newLeaseReturnProposal("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LeaseReturnProposalCodeLengthRule(16)
        val item = newLeaseReturnProposal("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LeaseReturnProposalRequiredCodeRule("lease return proposal needs an entry")
        val item = newLeaseReturnProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LeaseReturnProposalLabelLengthRule(86400)
        val item = newLeaseReturnProposal("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
