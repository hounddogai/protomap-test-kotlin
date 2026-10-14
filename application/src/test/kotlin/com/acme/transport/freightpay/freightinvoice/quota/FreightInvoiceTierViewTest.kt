package com.acme.transport.freightpay.freightinvoice.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class FreightInvoiceTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFreightInvoiceTier("code-149", "name-365")
        val view = toFreightInvoiceTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFreightInvoiceTierViews(mutableListOf<FreightInvoiceTier>(item, item)).size, "views")
    }
}
