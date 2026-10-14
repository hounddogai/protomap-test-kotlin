package com.acme.transport.freightpay.freightinvoice.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientFreightInvoiceRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientFreightInvoiceRollup("code-454", "label-126")
        val view = toLenientFreightInvoiceRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientFreightInvoiceRollupViews(mutableListOf<LenientFreightInvoiceRollup>(item, item)).size, "views")
    }
}
