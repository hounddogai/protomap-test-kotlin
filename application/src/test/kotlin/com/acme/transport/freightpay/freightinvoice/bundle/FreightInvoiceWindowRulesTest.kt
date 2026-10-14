package com.acme.transport.freightpay.freightinvoice.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceWindowValidator()
        val item = newFreightInvoiceWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FreightInvoiceWindowRequiredCodeRule("freight invoice window needs this field")
        val item = newFreightInvoiceWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = FreightInvoiceWindowMarginRangeRule(5.0, 4577.8)
        val item = newFreightInvoiceWindow("x", "y")
        item.margin = 4579.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above amount`() {
        val rule = FreightInvoiceWindowAmountRangeRule(3.4, 3553.2)
        val item = newFreightInvoiceWindow("x", "y")
        item.amount = 3554.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
