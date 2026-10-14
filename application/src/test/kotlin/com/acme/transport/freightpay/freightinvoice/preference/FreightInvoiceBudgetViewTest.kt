package com.acme.transport.freightpay.freightinvoice.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class FreightInvoiceBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFreightInvoiceBudget("code-479", "label-129")
        val view = toFreightInvoiceBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFreightInvoiceBudgetViews(mutableListOf<FreightInvoiceBudget>(item, item)).size, "views")
    }
}
