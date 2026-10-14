package com.acme.transport.freightpay.freightinvoice.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class FreightInvoiceMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFreightInvoiceMetric("code-246", "name-379")
        val view = toFreightInvoiceMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFreightInvoiceMetricViews(mutableListOf<FreightInvoiceMetric>(item, item)).size, "views")
    }
}
