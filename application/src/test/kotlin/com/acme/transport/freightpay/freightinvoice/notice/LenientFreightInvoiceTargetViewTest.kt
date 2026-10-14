package com.acme.transport.freightpay.freightinvoice.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientFreightInvoiceTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientFreightInvoiceTarget("code-128", "name-247")
        val view = toLenientFreightInvoiceTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientFreightInvoiceTargetViews(mutableListOf<LenientFreightInvoiceTarget>(item, item)).size, "views")
    }
}
