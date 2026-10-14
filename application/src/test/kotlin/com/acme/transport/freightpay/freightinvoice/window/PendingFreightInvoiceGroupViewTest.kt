package com.acme.transport.freightpay.freightinvoice.window

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingFreightInvoiceGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingFreightInvoiceGroup("code-905", "title-469")
        val view = toPendingFreightInvoiceGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingFreightInvoiceGroupViews(mutableListOf<PendingFreightInvoiceGroup>(item, item)).size, "views")
    }
}
