package com.acme.transport.freightpay.freightinvoice.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class FreightInvoiceNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFreightInvoiceNotice("code-161", "name-390")
        val view = toFreightInvoiceNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFreightInvoiceNoticeViews(mutableListOf<FreightInvoiceNotice>(item, item)).size, "views")
    }
}
