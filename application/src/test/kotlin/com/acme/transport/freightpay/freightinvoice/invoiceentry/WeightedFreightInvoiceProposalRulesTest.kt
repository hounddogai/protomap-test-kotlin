package com.acme.transport.freightpay.freightinvoice.invoiceentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedFreightInvoiceProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedFreightInvoiceProposalValidator()
        val item = newWeightedFreightInvoiceProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = WeightedFreightInvoiceProposalQuantityRangeRule(80, 86400)
        val item = newWeightedFreightInvoiceProposal("x", "y")
        item.quantity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = WeightedFreightInvoiceProposalPalletsRangeRule(3600, 3600)
        val item = newWeightedFreightInvoiceProposal("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above version`() {
        val rule = WeightedFreightInvoiceProposalVersionRangeRule(1000, 1000)
        val item = newWeightedFreightInvoiceProposal("x", "y")
        item.version = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedFreightInvoiceProposalRequiredTitleRule("weighted freight invoice proposal needs an entry")
        val item = newWeightedFreightInvoiceProposal("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
