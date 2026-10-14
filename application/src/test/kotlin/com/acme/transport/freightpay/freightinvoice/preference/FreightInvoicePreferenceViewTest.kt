package com.acme.transport.freightpay.freightinvoice.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class FreightInvoicePreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFreightInvoicePreference("code-923", "label-115")
        val view = toFreightInvoicePreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFreightInvoicePreferenceViews(mutableListOf<FreightInvoicePreference>(item, item)).size, "views")
    }
}
