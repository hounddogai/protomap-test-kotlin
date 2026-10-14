package com.acme.transport.freightpay.freightinvoice.invoicerollup

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalFreightInvoiceRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalFreightInvoiceRollup("code-168", "name-892")
        val view = toGlobalFreightInvoiceRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalFreightInvoiceRollupViews(mutableListOf<GlobalFreightInvoiceRollup>(item, item)).size, "views")
    }
}
