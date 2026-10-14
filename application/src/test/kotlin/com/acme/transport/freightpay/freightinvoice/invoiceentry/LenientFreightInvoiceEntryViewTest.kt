package com.acme.transport.freightpay.freightinvoice.invoiceentry

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientFreightInvoiceEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientFreightInvoiceEntry("code-658", "name-851")
        val view = toLenientFreightInvoiceEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientFreightInvoiceEntryViews(mutableListOf<LenientFreightInvoiceEntry>(item, item)).size, "views")
    }
}
