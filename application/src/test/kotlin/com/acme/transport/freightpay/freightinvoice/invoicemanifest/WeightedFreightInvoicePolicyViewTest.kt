package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedFreightInvoicePolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedFreightInvoicePolicy("code-983", "label-29")
        val view = toWeightedFreightInvoicePolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedFreightInvoicePolicyViews(mutableListOf<WeightedFreightInvoicePolicy>(item, item)).size, "views")
    }
}
