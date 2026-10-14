package com.acme.transport.freightpay.accessorial.accessorialcycle

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialLedger("code-622", "title-610")
        val view = toAccessorialLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialLedgerViews(mutableListOf<AccessorialLedger>(item, item)).size, "views")
    }
}
