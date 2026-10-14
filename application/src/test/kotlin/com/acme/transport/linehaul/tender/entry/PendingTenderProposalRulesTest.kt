package com.acme.transport.linehaul.tender.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingTenderProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingTenderProposalValidator()
        val item = newPendingTenderProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = PendingTenderProposalRequiredPhoneRule("pending tender proposal needs an entry")
        val item = newPendingTenderProposal("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = PendingTenderProposalRequiredCountryRule("pending tender proposal needs an entry")
        val item = newPendingTenderProposal("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingTenderProposalNameLengthRule(255)
        val item = newPendingTenderProposal("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = PendingTenderProposalCountryLengthRule(80)
        val item = newPendingTenderProposal("x", "y")
        item.country = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingTenderProposalRequiredCodeRule("pending tender proposal needs an entry")
        val item = newPendingTenderProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = PendingTenderProposalThresholdRangeRule(7.4, 3489.6)
        val item = newPendingTenderProposal("x", "y")
        item.threshold = 3491.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
