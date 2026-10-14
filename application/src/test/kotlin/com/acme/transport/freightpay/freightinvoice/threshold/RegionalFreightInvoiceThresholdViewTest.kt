package com.acme.transport.freightpay.freightinvoice.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalFreightInvoiceThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalFreightInvoiceThreshold("code-279", "title-347")
        val view = toRegionalFreightInvoiceThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalFreightInvoiceThresholdViews(mutableListOf<RegionalFreightInvoiceThreshold>(item, item)).size, "views")
    }
}
