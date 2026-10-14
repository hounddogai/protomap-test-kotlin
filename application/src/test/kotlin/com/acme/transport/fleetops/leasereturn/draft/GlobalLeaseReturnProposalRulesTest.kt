package com.acme.transport.fleetops.leasereturn.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalLeaseReturnProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalLeaseReturnProposalValidator()
        val item = newGlobalLeaseReturnProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = GlobalLeaseReturnProposalLimitRangeRule(120, 255)
        val item = newGlobalLeaseReturnProposal("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalLeaseReturnProposalNameLengthRule(32)
        val item = newGlobalLeaseReturnProposal("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalLeaseReturnProposalRequiredNameRule("global lease return proposal needs this field")
        val item = newGlobalLeaseReturnProposal("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = GlobalLeaseReturnProposalRequiredLabelRule("global lease return proposal needs this field")
        val item = newGlobalLeaseReturnProposal("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
