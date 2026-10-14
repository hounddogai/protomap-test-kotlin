package com.acme.transport.freightpay.freightinvoice.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingFreightInvoiceBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingFreightInvoiceBudget("code-286", "title-838")
        val view = toPendingFreightInvoiceBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingFreightInvoiceBudgetViews(mutableListOf<PendingFreightInvoiceBudget>(item, item)).size, "views")
    }
}
