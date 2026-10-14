package com.acme.transport.freightpay.freightinvoice.invoicereview

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualFreightInvoiceReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualFreightInvoiceReview("code-623", "name-999")
        val view = toManualFreightInvoiceReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualFreightInvoiceReviewViews(mutableListOf<ManualFreightInvoiceReview>(item, item)).size, "views")
    }
}
