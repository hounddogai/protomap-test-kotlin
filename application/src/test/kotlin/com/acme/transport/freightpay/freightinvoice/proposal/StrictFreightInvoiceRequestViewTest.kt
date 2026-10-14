package com.acme.transport.freightpay.freightinvoice.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictFreightInvoiceRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictFreightInvoiceRequest("code-216", "name-859")
        val view = toStrictFreightInvoiceRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictFreightInvoiceRequestViews(mutableListOf<StrictFreightInvoiceRequest>(item, item)).size, "views")
    }
}
