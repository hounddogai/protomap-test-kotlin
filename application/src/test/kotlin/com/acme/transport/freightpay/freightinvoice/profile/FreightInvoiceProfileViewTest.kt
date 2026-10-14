package com.acme.transport.freightpay.freightinvoice.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class FreightInvoiceProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFreightInvoiceProfile("code-14", "title-497")
        val view = toFreightInvoiceProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFreightInvoiceProfileViews(mutableListOf<FreightInvoiceProfile>(item, item)).size, "views")
    }
}
