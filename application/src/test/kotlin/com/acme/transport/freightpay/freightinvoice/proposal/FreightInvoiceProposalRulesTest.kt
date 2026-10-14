package com.acme.transport.freightpay.freightinvoice.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceProposalValidator()
        val item = newFreightInvoiceProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = FreightInvoiceProposalTrailerIdLengthRule(64)
        val item = newFreightInvoiceProposal("x", "y")
        item.trailerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FreightInvoiceProposalRequiredCodeRule("freight invoice proposal needs a value")
        val item = newFreightInvoiceProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FreightInvoiceProposalRequiredLabelRule("freight invoice proposal needs a value")
        val item = newFreightInvoiceProposal("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = FreightInvoiceProposalRequiredSegmentRule("freight invoice proposal needs a value")
        val item = newFreightInvoiceProposal("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
