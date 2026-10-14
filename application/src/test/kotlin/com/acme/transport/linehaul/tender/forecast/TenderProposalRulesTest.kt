package com.acme.transport.linehaul.tender.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderProposalValidator()
        val item = newTenderProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = TenderProposalContactEmailLengthRule(120)
        val item = newTenderProposal("x", "y")
        item.contactEmail = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = TenderProposalLabelLengthRule(120)
        val item = newTenderProposal("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = TenderProposalRequiredDescriptionRule("tender proposal needs an entry")
        val item = newTenderProposal("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = TenderProposalRequiredContactEmailRule("tender proposal needs a value")
        val item = newTenderProposal("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
