package com.acme.transport.freightpay.freightinvoice.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class FreightInvoiceBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFreightInvoiceBundle("code-536", "name-111")
        val view = toFreightInvoiceBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFreightInvoiceBundleViews(mutableListOf<FreightInvoiceBundle>(item, item)).size, "views")
    }
}
