package com.acme.transport.freightpay.freightinvoice.window

import kotlin.test.Test
import kotlin.test.assertEquals

class FreightInvoiceThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFreightInvoiceThreshold("code-298", "label-544")
        val view = toFreightInvoiceThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFreightInvoiceThresholdViews(mutableListOf<FreightInvoiceThreshold>(item, item)).size, "views")
    }
}
