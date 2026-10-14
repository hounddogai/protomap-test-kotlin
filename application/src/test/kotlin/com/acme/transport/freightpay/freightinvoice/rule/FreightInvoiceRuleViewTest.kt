package com.acme.transport.freightpay.freightinvoice.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class FreightInvoiceRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFreightInvoiceRule("code-888", "name-174")
        val view = toFreightInvoiceRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFreightInvoiceRuleViews(mutableListOf<FreightInvoiceRule>(item, item)).size, "views")
    }
}
