package com.acme.transport.freightpay.freightinvoice.score

import kotlin.test.Test
import kotlin.test.assertEquals

class FreightInvoiceScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFreightInvoiceScore("code-910", "name-437")
        val view = toFreightInvoiceScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFreightInvoiceScoreViews(mutableListOf<FreightInvoiceScore>(item, item)).size, "views")
    }
}
