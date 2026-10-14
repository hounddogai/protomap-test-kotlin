package com.acme.transport.freightpay.freightinvoice.window

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientFreightInvoiceDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientFreightInvoiceDraft("code-2", "label-118")
        val view = toLenientFreightInvoiceDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientFreightInvoiceDraftViews(mutableListOf<LenientFreightInvoiceDraft>(item, item)).size, "views")
    }
}
