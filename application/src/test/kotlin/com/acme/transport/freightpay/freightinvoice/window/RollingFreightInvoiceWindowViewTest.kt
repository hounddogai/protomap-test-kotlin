package com.acme.transport.freightpay.freightinvoice.window

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingFreightInvoiceWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingFreightInvoiceWindow("code-889", "label-867")
        val view = toRollingFreightInvoiceWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingFreightInvoiceWindowViews(mutableListOf<RollingFreightInvoiceWindow>(item, item)).size, "views")
    }
}
