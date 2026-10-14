package com.acme.transport.freightpay.freightinvoice.invoicereview

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientFreightInvoiceProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientFreightInvoiceProfile("code-355", "name-380")
        val view = toLenientFreightInvoiceProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientFreightInvoiceProfileViews(mutableListOf<LenientFreightInvoiceProfile>(item, item)).size, "views")
    }
}
