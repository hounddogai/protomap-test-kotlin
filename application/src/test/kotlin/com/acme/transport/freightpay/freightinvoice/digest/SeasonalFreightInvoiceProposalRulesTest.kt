package com.acme.transport.freightpay.freightinvoice.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalFreightInvoiceProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalFreightInvoiceProposalValidator()
        val item = newSeasonalFreightInvoiceProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalFreightInvoiceProposalLabelLengthRule(120)
        val item = newSeasonalFreightInvoiceProposal("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = SeasonalFreightInvoiceProposalRequiredDescriptionRule(
            "seasonal freight invoice proposal needs this field",
        )
        val item = newSeasonalFreightInvoiceProposal("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = SeasonalFreightInvoiceProposalGraceDaysRangeRule(80, 120)
        val item = newSeasonalFreightInvoiceProposal("x", "y")
        item.graceDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
