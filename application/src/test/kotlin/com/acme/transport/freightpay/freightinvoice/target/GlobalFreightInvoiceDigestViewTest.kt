package com.acme.transport.freightpay.freightinvoice.target

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalFreightInvoiceDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalFreightInvoiceDigest("code-722", "name-226")
        val view = toGlobalFreightInvoiceDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalFreightInvoiceDigestViews(mutableListOf<GlobalFreightInvoiceDigest>(item, item)).size, "views")
    }
}
