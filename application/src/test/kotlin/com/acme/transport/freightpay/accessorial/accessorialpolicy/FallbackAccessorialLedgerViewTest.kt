package com.acme.transport.freightpay.accessorial.accessorialpolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackAccessorialLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackAccessorialLedger("code-229", "label-374")
        val view = toFallbackAccessorialLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackAccessorialLedgerViews(mutableListOf<FallbackAccessorialLedger>(item, item)).size, "views")
    }
}
