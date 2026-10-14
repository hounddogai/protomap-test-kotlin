package com.acme.transport.freightpay.freightinvoice.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackFreightInvoiceCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackFreightInvoiceCheckpoint("code-325", "name-731")
        val view = toFallbackFreightInvoiceCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackFreightInvoiceCheckpointViews(mutableListOf<FallbackFreightInvoiceCheckpoint>(item, item)).size, "views")
    }
}
