package com.acme.transport.freightpay.freightinvoice.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedFreightInvoiceOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedFreightInvoiceOverride("code-814", "label-104")
        val view = toBatchedFreightInvoiceOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedFreightInvoiceOverrideViews(mutableListOf<BatchedFreightInvoiceOverride>(item, item)).size, "views")
    }
}
