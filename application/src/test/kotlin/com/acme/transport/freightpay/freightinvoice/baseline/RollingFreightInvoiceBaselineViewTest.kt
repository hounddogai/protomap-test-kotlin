package com.acme.transport.freightpay.freightinvoice.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingFreightInvoiceBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingFreightInvoiceBaseline("code-836", "title-987")
        val view = toRollingFreightInvoiceBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingFreightInvoiceBaselineViews(mutableListOf<RollingFreightInvoiceBaseline>(item, item)).size, "views")
    }
}
