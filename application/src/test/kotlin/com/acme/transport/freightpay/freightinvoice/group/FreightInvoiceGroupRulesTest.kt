package com.acme.transport.freightpay.freightinvoice.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceGroupValidator()
        val item = newFreightInvoiceGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FreightInvoiceGroupRequiredCodeRule("freight invoice group needs a value")
        val item = newFreightInvoiceGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FreightInvoiceGroupCodeLengthRule(86400)
        val item = newFreightInvoiceGroup("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
