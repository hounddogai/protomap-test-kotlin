package com.acme.transport.freightpay.freightinvoice.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class FreightInvoiceQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFreightInvoiceQuota("code-961", "label-848")
        val view = toFreightInvoiceQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFreightInvoiceQuotaViews(mutableListOf<FreightInvoiceQuota>(item, item)).size, "views")
    }
}
