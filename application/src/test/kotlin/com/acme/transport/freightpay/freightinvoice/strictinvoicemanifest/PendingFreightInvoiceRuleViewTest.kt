package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingFreightInvoiceRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingFreightInvoiceRule("code-687", "title-834")
        val view = toPendingFreightInvoiceRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingFreightInvoiceRuleViews(mutableListOf<PendingFreightInvoiceRule>(item, item)).size, "views")
    }
}
