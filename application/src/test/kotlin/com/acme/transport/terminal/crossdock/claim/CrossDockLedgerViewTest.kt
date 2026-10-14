package com.acme.transport.terminal.crossdock.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockLedger("code-203", "title-201")
        val view = toCrossDockLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockLedgerViews(mutableListOf<CrossDockLedger>(item, item)).size, "views")
    }
}
