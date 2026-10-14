package com.acme.transport.freightpay.freightinvoice.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class FreightInvoiceAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFreightInvoiceAdjustment("code-384", "name-598")
        val view = toFreightInvoiceAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFreightInvoiceAdjustmentViews(mutableListOf<FreightInvoiceAdjustment>(item, item)).size, "views")
    }
}
