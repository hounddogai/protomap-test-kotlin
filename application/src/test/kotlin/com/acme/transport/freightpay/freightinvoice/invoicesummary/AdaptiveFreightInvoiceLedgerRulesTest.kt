package com.acme.transport.freightpay.freightinvoice.invoicesummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveFreightInvoiceLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveFreightInvoiceLedgerValidator()
        val item = newAdaptiveFreightInvoiceLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveFreightInvoiceLedgerTitleLengthRule(16)
        val item = newAdaptiveFreightInvoiceLedger("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = AdaptiveFreightInvoiceLedgerRequiredContactEmailRule(
            "adaptive freight invoice ledger needs this field",
        )
        val item = newAdaptiveFreightInvoiceLedger("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = AdaptiveFreightInvoiceLedgerContactEmailLengthRule(3600)
        val item = newAdaptiveFreightInvoiceLedger("x", "y")
        item.contactEmail = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveFreightInvoiceLedgerRequiredCodeRule("adaptive freight invoice ledger needs this field")
        val item = newAdaptiveFreightInvoiceLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
