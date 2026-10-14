package com.acme.transport.freightpay.freightinvoice.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledFreightInvoiceSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledFreightInvoiceSnapshot("code-982", "title-664")
        val view = toScheduledFreightInvoiceSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledFreightInvoiceSnapshotViews(mutableListOf<ScheduledFreightInvoiceSnapshot>(item, item)).size, "views")
    }
}
