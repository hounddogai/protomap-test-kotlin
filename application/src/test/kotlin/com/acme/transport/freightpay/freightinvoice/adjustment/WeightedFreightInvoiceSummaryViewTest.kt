package com.acme.transport.freightpay.freightinvoice.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedFreightInvoiceSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedFreightInvoiceSummary("code-580", "name-288")
        val view = toWeightedFreightInvoiceSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedFreightInvoiceSummaryViews(mutableListOf<WeightedFreightInvoiceSummary>(item, item)).size, "views")
    }
}
