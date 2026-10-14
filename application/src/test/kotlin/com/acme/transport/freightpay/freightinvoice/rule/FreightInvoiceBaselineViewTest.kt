package com.acme.transport.freightpay.freightinvoice.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class FreightInvoiceBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFreightInvoiceBaseline("code-150", "label-216")
        val view = toFreightInvoiceBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFreightInvoiceBaselineViews(mutableListOf<FreightInvoiceBaseline>(item, item)).size, "views")
    }
}
