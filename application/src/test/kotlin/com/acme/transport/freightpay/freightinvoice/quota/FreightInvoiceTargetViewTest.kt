package com.acme.transport.freightpay.freightinvoice.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class FreightInvoiceTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFreightInvoiceTarget("code-714", "title-200")
        val view = toFreightInvoiceTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFreightInvoiceTargetViews(mutableListOf<FreightInvoiceTarget>(item, item)).size, "views")
    }
}
