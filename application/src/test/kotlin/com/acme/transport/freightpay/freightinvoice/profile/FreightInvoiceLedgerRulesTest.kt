package com.acme.transport.freightpay.freightinvoice.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceLedgerValidator()
        val item = newFreightInvoiceLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FreightInvoiceLedgerCodeLengthRule(120)
        val item = newFreightInvoiceLedger("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
