package com.acme.transport.freightpay.freightinvoice.invoicequota

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingFreightInvoiceQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingFreightInvoiceQuota("code-774", "name-850")
        val view = toRollingFreightInvoiceQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingFreightInvoiceQuotaViews(mutableListOf<RollingFreightInvoiceQuota>(item, item)).size, "views")
    }
}
