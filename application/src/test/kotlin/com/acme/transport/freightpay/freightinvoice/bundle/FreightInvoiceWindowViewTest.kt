package com.acme.transport.freightpay.freightinvoice.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class FreightInvoiceWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFreightInvoiceWindow("code-97", "label-149")
        val view = toFreightInvoiceWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFreightInvoiceWindowViews(mutableListOf<FreightInvoiceWindow>(item, item)).size, "views")
    }
}
