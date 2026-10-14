package com.acme.transport.freightpay.freightinvoice.invoicesummary

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveFreightInvoiceLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveFreightInvoiceLedger("code-270", "title-579")
        val view = toAdaptiveFreightInvoiceLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveFreightInvoiceLedgerViews(mutableListOf<AdaptiveFreightInvoiceLedger>(item, item)).size, "views")
    }
}
