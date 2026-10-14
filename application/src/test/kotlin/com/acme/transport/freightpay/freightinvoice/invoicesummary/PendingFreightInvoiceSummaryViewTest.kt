package com.acme.transport.freightpay.freightinvoice.invoicesummary

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingFreightInvoiceSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingFreightInvoiceSummary("code-655", "label-301")
        val view = toPendingFreightInvoiceSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingFreightInvoiceSummaryViews(mutableListOf<PendingFreightInvoiceSummary>(item, item)).size, "views")
    }
}
