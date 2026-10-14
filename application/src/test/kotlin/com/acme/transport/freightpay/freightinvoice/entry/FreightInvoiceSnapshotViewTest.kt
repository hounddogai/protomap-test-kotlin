package com.acme.transport.freightpay.freightinvoice.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class FreightInvoiceSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFreightInvoiceSnapshot("code-181", "name-582")
        val view = toFreightInvoiceSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFreightInvoiceSnapshotViews(mutableListOf<FreightInvoiceSnapshot>(item, item)).size, "views")
    }
}
