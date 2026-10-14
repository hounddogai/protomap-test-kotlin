package com.acme.transport.freightpay.freightinvoice.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalFreightInvoiceSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalFreightInvoiceSummary("code-977", "title-971")
        val view = toRegionalFreightInvoiceSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalFreightInvoiceSummaryViews(mutableListOf<RegionalFreightInvoiceSummary>(item, item)).size, "views")
    }
}
