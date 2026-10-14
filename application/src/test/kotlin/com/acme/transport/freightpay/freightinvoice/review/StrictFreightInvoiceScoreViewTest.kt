package com.acme.transport.freightpay.freightinvoice.review

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictFreightInvoiceScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictFreightInvoiceScore("code-499", "title-238")
        val view = toStrictFreightInvoiceScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictFreightInvoiceScoreViews(mutableListOf<StrictFreightInvoiceScore>(item, item)).size, "views")
    }
}
