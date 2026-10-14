package com.acme.transport.freightpay.freightinvoice.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyFreightInvoiceRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyFreightInvoiceRule("code-995", "title-287")
        val view = toLegacyFreightInvoiceRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyFreightInvoiceRuleViews(mutableListOf<LegacyFreightInvoiceRule>(item, item)).size, "views")
    }
}
